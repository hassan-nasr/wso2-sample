/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cassandra.tools;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.cli.*;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Directories;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.db.compaction.LeveledManifest;
import org.apache.cassandra.db.compaction.SSTableSplitter;
import org.apache.cassandra.io.sstable.*;
import org.apache.cassandra.service.CassandraDaemon;
import org.apache.cassandra.utils.Pair;

import static org.apache.cassandra.tools.BulkLoader.CmdLineOptions;

public class StandaloneSplitter
{
    public static final int DEFAULT_SSTABLE_SIZE = 50;

    static
    {
        CassandraDaemon.initLog4j();
    }

    private static final String TOOL_NAME = "sstablessplit";
    private static final String VERBOSE_OPTION = "verbose";
    private static final String DEBUG_OPTION = "debug";
    private static final String HELP_OPTION = "help";
    private static final String NO_SNAPSHOT_OPTION = "no-snapshot";
    private static final String SIZE_OPTION = "size";

    public static void main(String args[]) throws IOException
    {
        Options options = Options.parseArgs(args);
        try
        {
            // load keyspace descriptions.
            DatabaseDescriptor.loadSchemas();

            String ksName = null;
            String cfName = null;
            Map<Descriptor, Set<Component>> parsedFilenames = new HashMap<Descriptor, Set<Component>>();
            for (String filename : options.filenames)
            {
                File file = new File(filename);
                if (!file.exists()) {
                    System.out.println("Skipping inexisting file " + file);
                    continue;
                }

                Pair<Descriptor, Component> pair = SSTable.tryComponentFromFilename(file.getParentFile(), file.getName());
                if (pair == null) {
                    System.out.println("Skipping non sstable file " + file);
                    continue;
                }
                Descriptor desc = pair.left;

                if (ksName == null)
                    ksName = desc.ksname;
                else if (!ksName.equals(desc.ksname))
                    throw new IllegalArgumentException("All sstables must be part of the same keyspace");

                if (cfName == null)
                    cfName = desc.cfname;
                else if (!cfName.equals(desc.cfname))
                    throw new IllegalArgumentException("All sstables must be part of the same column family");

                Set<Component> components = new HashSet<Component>(Arrays.asList(new Component[]{
                    Component.DATA,
                    Component.PRIMARY_INDEX,
                    Component.FILTER,
                    Component.COMPRESSION_INFO,
                    Component.STATS
                }));

                Iterator<Component> iter = components.iterator();
                while (iter.hasNext()) {
                    Component component = iter.next();
                    if (!(new File(desc.filenameFor(component)).exists()))
                        iter.remove();
                }
                parsedFilenames.put(desc, components);
            }

            if (ksName == null || cfName == null)
            {
                System.err.println("No valid sstables to split");
                System.exit(1);
            }

            // Do not load sstables since they might be broken
            Table table = Table.openWithoutSSTables(ksName);
            ColumnFamilyStore cfs = table.getColumnFamilyStore(cfName);

            String snapshotName = "pre-split-" + System.currentTimeMillis();

            List<SSTableReader> sstables = new ArrayList<SSTableReader>();
            for (Map.Entry<Descriptor, Set<Component>> fn : parsedFilenames.entrySet())
            {
                try
                {
                    SSTableReader sstable = SSTableReader.openNoValidation(fn.getKey(), fn.getValue(), cfs.metadata);
                    sstables.add(sstable);

                    if (options.snapshot) {
                        File snapshotDirectory = Directories.getSnapshotDirectory(sstable.descriptor, snapshotName);
                        sstable.createLinks(snapshotDirectory.getPath());
                    }

                }
                catch (Exception e)
                {
                    System.err.println(String.format("Error Loading %s: %s", fn.getKey(), e.getMessage()));
                    if (options.debug)
                        e.printStackTrace(System.err);
                }
            }
            if (options.snapshot)
                System.out.println(String.format("Pre-split sstables snapshotted into snapshot %s", snapshotName));

            cfs.getDataTracker().markCompacting(sstables);
            for (SSTableReader sstable : sstables)
            {
                try
                {
                    new SSTableSplitter(cfs, sstable, options.sizeInMB).split();

                    // Remove the sstable
                    sstable.markCompacted();
                    sstable.releaseReference();
                }
                catch (Exception e)
                {
                    System.err.println(String.format("Error splitting %s: %s", sstable, e.getMessage()));
                    if (options.debug)
                        e.printStackTrace(System.err);
                }
            }
            SSTableDeletingTask.waitForDeletions();
            System.exit(0); // We need that to stop non daemonized threads
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
            if (options.debug)
                e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static class Options
    {
        public final List<String> filenames;

        public boolean debug;
        public boolean verbose;
        public boolean snapshot;
        public int sizeInMB;

        private Options(List<String> filenames)
        {
            this.filenames = filenames;
        }

        public static Options parseArgs(String cmdArgs[])
        {
            CommandLineParser parser = new GnuParser();
            CmdLineOptions options = getCmdLineOptions();
            try
            {
                CommandLine cmd = parser.parse(options, cmdArgs, false);

                if (cmd.hasOption(HELP_OPTION))
                {
                    printUsage(options);
                    System.exit(0);
                }

                String[] args = cmd.getArgs();
                if (args.length == 0)
                {
                    System.err.println("No sstables to split");
                    printUsage(options);
                    System.exit(1);
                }
                Options opts = new Options(Arrays.asList(args));
                opts.debug = cmd.hasOption(DEBUG_OPTION);
                opts.verbose = cmd.hasOption(VERBOSE_OPTION);
                opts.snapshot = !cmd.hasOption(NO_SNAPSHOT_OPTION);
                opts.sizeInMB = DEFAULT_SSTABLE_SIZE;

                if (cmd.hasOption(SIZE_OPTION))
                    opts.sizeInMB = Integer.valueOf(cmd.getOptionValue(SIZE_OPTION));

                return opts;
            }
            catch (ParseException e)
            {
                errorMsg(e.getMessage(), options);
                return null;
            }
        }

        private static void errorMsg(String msg, CmdLineOptions options)
        {
            System.err.println(msg);
            printUsage(options);
            System.exit(1);
        }

        private static CmdLineOptions getCmdLineOptions()
        {
            CmdLineOptions options = new CmdLineOptions();
            options.addOption(null, DEBUG_OPTION,          "display stack traces");
            options.addOption("v",  VERBOSE_OPTION,        "verbose output");
            options.addOption("h",  HELP_OPTION,           "display this help message");
            options.addOption(null, NO_SNAPSHOT_OPTION,    "don't snapshot the sstables before splitting");
            options.addOption("s",  SIZE_OPTION, "size",   "maximum size in MB for the output sstables (default: " + DEFAULT_SSTABLE_SIZE + ")");
            return options;
        }

        public static void printUsage(CmdLineOptions options)
        {
            String usage = String.format("%s [options] <filename> [<filename>]*", TOOL_NAME);
            StringBuilder header = new StringBuilder();
            header.append("--\n");
            header.append("Split the provided sstables files in sstables of maximum provided file size (see option --" + SIZE_OPTION + ")." );
            header.append("\n--\n");
            header.append("Options are:");
            new HelpFormatter().printHelp(usage, header.toString(), options, "");
        }
    }
}
