package com.sergius.bitbucket.exporter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CommandLineOptionsParser {

    private static final List<String> HELP = Arrays.asList( "help", "h", "?" );

    private static final List<String> HOST = Arrays.asList("server-host", "host", "H");
    private static final List<String> USERNAME = Arrays.asList("username", "user", "u");
    private static final List<String> PASSWORD = Arrays.asList("password", "pass", "p");
    private static final List<String> DIRECTORY = Arrays.asList("dir", "out", "o", "d");
    private static final List<String> BARE = Arrays.asList("bare", "b");

    public static Options parse(final String[] args) {
        final OptionParser parser = new OptionParser();

        final OptionSpec<Void> help = parser.acceptsAll(HELP, "show help").forHelp();

        final OptionSpec<String> host = createSpec(parser, help, HOST, "bitbucket server host");
        final OptionSpec<String> username = createSpec(parser, help, USERNAME, "name of user with access right to specified bitbucket server");
        final OptionSpec<String> password = createSpec(parser, help, PASSWORD, "password or specified user");

        final OptionSpec<File> directory = parser.acceptsAll(DIRECTORY, "output directory for exported repositories").withRequiredArg().ofType(File.class);
        final OptionSpec<Boolean> bare = parser.acceptsAll(BARE, "create bare copy").withRequiredArg().ofType(Boolean.class);

        OptionSet options;

        try {
            options = parser.parse(args);
        } catch (final Exception e) {
            System.out.println(e.getMessage());
            System.out.println();

            printHelp(parser);
            return null;
        }

        if (options.has(help)) {
            printHelp(parser);
        }

        return Options.builder()
               .host(host.value(options))
               .username(username.value(options))
               .password(password.value(options))
               .bare(options.has(bare) ? bare.value(options) : false)
               .dir(options.has(directory) ? directory.value(options) : new File(""))
               .build();
    }

    private static void printHelp(final OptionParser parser) {
        try {
            System.out.println("Usage:");
            System.out.println("java -jar bitbucket-exporter.jar --user=[user_name] --pass=[password] --host=[bitbucket_host]");
            System.out.println();
            System.out.println("Supported options:");

            parser.printHelpOn(System.out);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static OptionSpec<String> createSpec(final OptionParser parser, final OptionSpec<Void> help,
                                                 final List<String> optionNames, final String desctiption) {
        return parser.acceptsAll(optionNames, desctiption)
                     .requiredUnless(help)
                     .withRequiredArg()
                     .required()
                     .ofType(String.class);
    }
}
