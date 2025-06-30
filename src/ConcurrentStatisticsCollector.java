import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.*;

public class ConcurrentStatisticsCollector {
    // Usar ConcurrentMap para segurança de thread
    private ConcurrentMap<DownloadKey, DownloadStats> statsMap = new ConcurrentHashMap<>();

    /**
     * Adiciona um tempo de download para uma tripla de características.
     * Este método é seguro para threads.
     * @param chunkSize O tamanho do chunk.
     * @param numberOfPeers A quantidade de peers.
     * @param fileSize O tamanho do arquivo.
     * @param downloadTime O tempo de download.
     */
    public void addDownloadTime(int chunkSize, int numberOfPeers, long fileSize, long downloadTime) {
        DownloadKey key = new DownloadKey(chunkSize, numberOfPeers, fileSize);
        // Usa computeIfAbsent para criar uma nova entrada se a chave ainda não existir
        statsMap.computeIfAbsent(key, k -> new DownloadStats()).addDownloadTime(downloadTime);
    }

    // O método displayStatistics() seria o mesmo da classe anterior
    public void displayStatistics() {
        // Adjusted header for better alignment with the data
        System.out.println("Tam. chunk | N peers | Tam. arquivo | N | Tempo [s] | Desvio");
        // Original: System.out.println("Tam. chunk | N peers | Tam. arquivo | N | Tempo [s] | Desvio");

        for (Map.Entry<DownloadKey, DownloadStats> entry : statsMap.entrySet()) {
            DownloadKey key = entry.getKey();
            DownloadStats stats = entry.getValue();

            // Adjusted format string for better alignment and data types
            // Assuming getFileSize returns a long/int, getSampleSize returns an int,
            // getAverageTime and getStandardDeviation return doubles.
            System.out.printf("%-10d | %-7d | %-12d | %-7d | %-10.6f | %-10.6f %n",
                            key.getChunkSize(),
                            key.getNumberOfPeers(),
                            key.getFileSize(), // Assuming this returns a long/int like 4900000
                            stats.getSampleSize(), // Assuming this returns an int
                            stats.getAverageTime(), // Assuming this returns a double
                            stats.getStandardDeviation()); // Assuming this returns a double
        }
    }
}