package dev.felicityiris.metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CpuCollector implements MetricCollector {
    private long previousIdle = -1;
    private long previousTotal = -1;

    @Override
    public String getMetricID() {
        return "cpu.usage";
    }

    @Override
    public double collect() {
        try {
            String line = Files.readAllLines(Path.of("/proc/stat"))
                    .stream()
                    .filter(l -> l.startsWith("cpu "))
                    .findFirst()
                    .orElseThrow();
            String[] parts = line.trim().split("\\s+");

            long user = Long.parseLong(parts[1]);
            long nice = Long.parseLong(parts[2]);
            long system = Long.parseLong(parts[3]);
            long idle = Long.parseLong(parts[4]);
            long iowait = Long.parseLong(parts[5]);
            long irq = Long.parseLong(parts[6]);
            long softirq = Long.parseLong(parts[7]);
            long steal = Long.parseLong(parts[8]);

            long idleTime = idle + iowait;

            long totalTime = user + nice + system + idle + iowait + irq + softirq + steal;

            if (previousTotal == -1) {
                previousIdle = idleTime;
                previousTotal = totalTime;
                return 0.0;
            }

            long idleDelta = idleTime - previousIdle;
            long totalDelta = totalTime - previousTotal;

            if (totalDelta == 0) {
                return 0.0;
            }

            return (1.0 - ((double) idleDelta / totalDelta)) * 100.0;
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Failed to read CPU Statistics\n", e);
        }
    }
}
