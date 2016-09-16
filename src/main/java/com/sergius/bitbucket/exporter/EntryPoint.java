package com.sergius.bitbucket.exporter;

import java.util.List;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EntryPoint {

    public static void main(final String[] args) {
        final Options options = CommandLineOptionsParser.parse(args);

        if (options != null) {
            final List<BitbucketProject> projects = BitbucketRepositories.getUrls(options);

            GitRepositoryManager.clone(projects, options);
        }
    }
}
