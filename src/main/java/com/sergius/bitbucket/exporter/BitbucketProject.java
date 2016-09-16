package com.sergius.bitbucket.exporter;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class BitbucketProject {
    private final String name;
    private List<ProjectRepo> repos;

    @Getter
    @RequiredArgsConstructor
    public static class ProjectRepo {
        private final String name;
        private final String url;
    }
}
