package com.sergius.bitbucket.exporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sergius.bitbucket.exporter.BitbucketProject.ProjectRepo;
import com.sergius.bitbucket.exporter.BitbucketRepositories.Projects.Project;
import com.sergius.bitbucket.exporter.BitbucketRepositories.Repos.Repo;
import com.sergius.bitbucket.exporter.BitbucketRepositories.Repos.Repo.Links;
import com.sergius.bitbucket.exporter.BitbucketRepositories.Repos.Repo.Links.CloneUrl;

import lombok.Setter;
import lombok.experimental.UtilityClass;


@UtilityClass
public class BitbucketRepositories {
    private static final String HTTP = "http";
    private static final String SCHEME = "://";
    private static final String URL_SEPARATOR = "/";
    private static final String REST_URL = "rest/api/1.0";
    private static final String PROJECTS_URL = "projects";
    private static final String REPOS_URL = "repos";

    public static List<BitbucketProject> getUrls(final Options options) {
        final List<BitbucketProject> projectRepositories = new ArrayList<>();

        try {
            Unirest.setObjectMapper(new Mapper());

            final String projectsUrl = buildProjectsUrl(options);

            final HttpResponse<Projects> res = Unirest.get(projectsUrl)
                                                      .basicAuth(options.getUsername(), options.getPassword())
                                                      .asObject(Projects.class);

            final Projects projects = res.getBody();

            if (projects.values != null) {
                for (final Project project : projects.values) {
                    final BitbucketProject bp = getProjectWithRepositories(project, projectsUrl, options);

                    projectRepositories.add(bp);
                }
            } else {
                System.out.println(String.format("Projects for user %s not found", options.getUsername()));
            }

            Unirest.shutdown();
        } catch (final UnirestException | IOException e) {
            System.out.println(e.getMessage());
        }

        return projectRepositories;
    }

    private static BitbucketProject getProjectWithRepositories(final Project project, final String projectsUrl,
                                                               final Options options) throws UnirestException {
        final BitbucketProject bp = new BitbucketProject(project.key);

        final String projectReposUrl = buildProjectReposUrl(projectsUrl, project.key);

        final HttpResponse<Repos> reposRes = Unirest.get(projectReposUrl)
                        .basicAuth(options.getUsername(), options.getPassword()).asObject(Repos.class);

        final Repos repos = reposRes.getBody();

        bp.setRepos(getProjectRepositories(project.key, repos));

        return bp;
    }

    private static List<ProjectRepo> getProjectRepositories(final String project, final Repos repos) {
        final List<ProjectRepo> repositories = new ArrayList<>();

        if (repos.values != null) {
            for (final Repo repo : repos.values) {
                final ProjectRepo projectRepo = getProjectRepository(repo);

                if (projectRepo != null) {
                    repositories.add(projectRepo);
                }
            }
        } else {
            System.out.println(String.format("Repos for project %s not found", project));
        }

        return repositories;
    }

    private static ProjectRepo getProjectRepository(final Repo repo) {
        if (repo.links != null && repo.links.clone != null) {
            final Links links = repo.links;

            final Optional<CloneUrl> link = links.clone.stream().filter(cloneUrl -> HTTP.equals(cloneUrl.name)).findFirst();

            if (link.isPresent()) {
                return new ProjectRepo(repo.slug, link.get().href);
            } else {
                System.out.println(String.format("Http clone url of repo %s not defined", repo.slug));
            }
        } else {
            System.out.println(String.format("Clone links of repo %s not defined", repo.slug));
        }

        return null;
    }

    private static String buildProjectReposUrl(final String projectsUrl, final String project) {
        final StringBuilder url = new StringBuilder();

        url.append(projectsUrl)
           .append(URL_SEPARATOR)
           .append(project)
           .append(URL_SEPARATOR)
           .append(REPOS_URL);

        return url.toString();
    }

    private static String buildProjectsUrl(final Options options) {
        final String host = options.getHost();

        final StringBuilder url = new StringBuilder();

        if (!host.startsWith(HTTP)) {
            url.append(HTTP).append(SCHEME);
        }

        url.append(host);

        if (!host.endsWith(URL_SEPARATOR)) {
            url.append(URL_SEPARATOR);
        }

        url.append(REST_URL)
           .append(URL_SEPARATOR)
           .append(PROJECTS_URL);

        return url.toString();
    }

    public static class Projects {
        private List<Project> values;

        public void setValues(final List<Project> values) {
            this.values = values;
        }

        @Setter
        public static class Project {
            private String key;
        }
    }

    public static class Repos {
        private List<Repo> values;

        public void setValues(final List<Repo> values) {
            this.values = values;
        }

        public static class Repo {
            private String slug;
            private Links links;

            public void setSlug(final String slug) {
                this.slug = slug;
            }

            public void setLinks(final Links links) {
                this.links = links;
            }

            public static class Links {
                private List<CloneUrl> clone;

                public void setClone(final List<CloneUrl> clone) {
                    this.clone = clone;
                }

                @Setter
                public static class CloneUrl {
                    private String href;
                    private String name;
                }
            }
        }
    }

    private static class Mapper implements ObjectMapper {
        private final com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper;

        public Mapper() {
            jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            jacksonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        @Override
        public <T> T readValue(final String value, final Class<T> valueType) {
            try {
                return jacksonObjectMapper.readValue(value, valueType);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String writeValue(final Object value) {
            try {
                return jacksonObjectMapper.writeValueAsString(value);
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
