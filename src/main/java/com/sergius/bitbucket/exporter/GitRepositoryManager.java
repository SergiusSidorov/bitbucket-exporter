package com.sergius.bitbucket.exporter;

import java.io.File;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.sergius.bitbucket.exporter.BitbucketProject.ProjectRepo;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GitRepositoryManager {

    public static void clone(final List<BitbucketProject> projects, final Options options) {
        final CredentialsProvider credentials = new UsernamePasswordCredentialsProvider(options.getUsername(), options.getPassword());

        for (final BitbucketProject project : projects) {
            System.out.print(String.format("Clone repositories of project %s into directory: ", project.getName()));

            final String projectPath = buildPath(options.getDir().getAbsolutePath(), project.getName());

            createDirIfNotExist(projectPath);

            for (final ProjectRepo repo : project.getRepos()) {
                System.out.print(String.format("--> Cloning repository %s into directory: ", repo.getName()));

                final String repoPath = buildPath(projectPath, repo.getName());

                final File repoDir = createDirIfNotExist(repoPath);

                try {
                    Git.cloneRepository()
                       .setURI(repo.getUrl())
                       .setBare(options.isBare())
                       .setDirectory(repoDir)
                       .setCredentialsProvider(credentials)
                       .call();
                } catch (IllegalStateException | GitAPIException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private static String buildPath(final String basePath, final String dir) {
        return basePath + File.separator + dir;
    }

    private static File createDirIfNotExist(final String path) {
        System.out.println(path);

        final File dir = new File(path);

        if (!dir.exists()) {
            dir.mkdir();
        }

        return dir;
    }
}
