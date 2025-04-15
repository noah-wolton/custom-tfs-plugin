package io.jenkins.plugins.simpletfsscm;

import hudson.scm.ChangeLogParser;

public class SimpleTFSChangeLogParser extends ChangeLogParser {
    SimpleTFSChangeLogParser() { System.out.println("ChangeLogParser()"); }
}
