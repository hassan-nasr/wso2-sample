package org.wso2.carbon.adc.mgt.cli.utils;

/**
 * Constants for CLI Tool
 */
public class CliConstants {

	public static final String STRATOS_APPLICATION_NAME = "stratos";

	public static final String STRATOS_URL_ENV_PROPERTY = "STRATOS_URL";

	public static final String STRATOS_USERNAME_ENV_PROPERTY = "STRATOS_USERNAME";

	public static final String STRATOS_PASSWORD_ENV_PROPERTY = "STRATOS_PASSWORD";

	public static final String STRATOS_SHELL_PROMPT = "stratos> ";
	
	public static final int SUCCESSFUL_CODE = 0;
	public static final int BAD_ARGS_CODE = 1;
	public static final int ERROR_CODE = 2;


	/**
	 * The Directory for storing configuration
	 */
	public static final String STRATOS_DIR = ".stratos";
	public static final String STRATOS_HISTORY_DIR = ".history";

	public static final String HELP_ACTION = "help";

	/**
	 * Subscribe to a cartridge.
	 */
	public static final String SUBSCRIBE_ACTION = "subscribe";

	public static final String UNSUBSCRIBE_ACTION = "unsubscribe";

	/**
	 * List the subscribed cartridges
	 */
	public static final String LIST_ACTION = "list";

	/**
	 * List the available cartridges
	 */
	public static final String CARTRIDGES_ACTION = "cartridges";

	/**
	 * Give information of a cartridge.
	 */
	public static final String INFO_ACTION = "info";

	/**
	 * Synchronize repository
	 */
	public static final String SYNC_ACTION = "sync";

	/**
	 * Domain mapping
	 */
	public static final String ADD_DOMAIN_MAPPING_ACTION = "add-domain-mapping";
	/**
	 * Remove Domain mapping
	 */
	public static final String REMOVE_DOMAIN_MAPPING_ACTION = "remove-domain-mapping";
	
	/**
	 * List the available policies
	 */
	public static final String POLICIES_ACTION = "policies";

	/**
	 * Exit action
	 */
	public static final String EXIT_ACTION = "exit";

	public static final String REPO_URL_OPTION = "r";
	public static final String REPO_URL_LONG_OPTION = "repoURL";
	
	public static final String PRIVATE_REPO_OPTION = "i";
	public static final String PRIVATE_REPO_LONG_OPTION = "privateRepo";

	public static final String USERNAME_OPTION = "u";
	public static final String USERNAME_LONG_OPTION = "username";

	public static final String PASSWORD_OPTION = "p";
	public static final String PASSWORD_LONG_OPTION = "password";

	public static final String HELP_OPTION = "h";
	public static final String HELP_LONG_OPTION = "help";
	
	public static final String POLICY_OPTION = "o";
	public static final String POLICY_LONG_OPTION = "policy";
	
	public static final String CONNECT_OPTION = "c";
	public static final String CONNECT_LONG_OPTION = "connect";
	
	public static final String DATA_ALIAS_OPTION = "d";
	public static final String DATA_ALIAS_LONG_OPTION = "data-alias";
	
	public static final String FULL_OPTION = "f";
	public static final String FULL_LONG_OPTION = "full";
	
	public static final String FORCE_OPTION = "f";
	public static final String FORCE_LONG_OPTION = "force";
	
	public static final String TRACE_OPTION = "trace";
	
	public static final String DEBUG_OPTION = "debug";

}
