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
        System.out.printf("  %-12s | %-8s | %-13s | %-2s | %-10s | %-7s\n", "Tam. chunk", "N peers", "Tam. arquivo", "N", "Tempo [s]", "Desvio");

        for (Map.Entry<DownloadKey, DownloadStats> entry : statsMap.entrySet()) {
            DownloadKey key = entry.getKey();
            DownloadStats stats = entry.getValue();
            System.out.printf(" %-5d | %-3d | %-7d | %-2d | %-7f | %-7f ", key.getChunkSize(), key.getNumberOfPeers(), key.getFileSize(), stats.getSampleSize(), stats.getAverageTime(), stats.getStandardDeviation());
        }

    }
}