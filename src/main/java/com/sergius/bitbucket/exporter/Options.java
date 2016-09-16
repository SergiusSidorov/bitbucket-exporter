package com.sergius.bitbucket.exporter;

import java.io.File;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Options {
    private final String username;
    private final String password;
    private final String host;
    private final boolean bare;
    private final File dir;
}
