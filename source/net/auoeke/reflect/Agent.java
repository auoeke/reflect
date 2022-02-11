package net.auoeke.reflect;

import java.lang.instrument.Instrumentation;

class Agent {
    static Instrumentation instrumentation;

    public static void premain(String options, Instrumentation instrumentation) {
        Agent.instrumentation = instrumentation;
    }

    public static void agentmain(String options, Instrumentation instrumentation) {
        Agent.instrumentation = instrumentation;
    }
}