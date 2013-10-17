/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.registry.checkin.scm.checkin;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.wso2.registry.checkin.scm.Utils;
import org.wso2.registry.checkin.scm.repository.ProviderRepository;

public class CheckInCommand extends AbstractCheckInCommand {

    protected CheckInScmResult executeCheckInCommand(ScmProviderRepository scmProviderRepository,
                                                     ScmFileSet scmFileSet, String s,
                                                     ScmVersion scmVersion)
            throws ScmException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("executing check-in command...");
        }

        ProviderRepository repository = (ProviderRepository) scmProviderRepository;
        Commandline cl = Utils.getCommandLine(repository, "ci", scmFileSet);

        CheckInConsumer consumer = new CheckInConsumer(getLogger());

        CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Executing: " + cl.getWorkingDirectory().getAbsolutePath() + ">>" +
                    Utils.getCommandLineAsString(cl));
        }

        try {
            exitCode = CommandLineUtils.executeCommandLine(cl, consumer, err);
            if (exitCode != 0) {
                // print out the writable copy for manual handling
                if (getLogger().isWarnEnabled()) {
                    getLogger().warn(err.getOutput());
                }
            }
        } catch (CommandLineException e) {
            getLogger().error("An error occurred while performing check-in", e);
        }

        return new CheckInScmResult(cl.toString(), consumer.getUpdatedFiles());
    }
}
