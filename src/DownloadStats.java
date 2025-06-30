import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DownloadStats {
    private List<Long> downloadTimes; // Usar List para armazenar todos os tempos de download

    public DownloadStats() {
        this.downloadTimes = new ArrayList<>();
    }

    public void addDownloadTime(long time) {
        this.downloadTimes.add(time);
    }

    public List<Long> getDownloadTimes() {
        return downloadTimes;
    }

    public int getSampleSize(){
        return downloadTimes.size();
    }
    /**
     * Calcula a média dos tempos de download.
     * @return O tempo médio de download.
     */
    public double getAverageTime() {
        if (downloadTimes.isEmpty()) {
            return 0.0;
        }
        long sum = 0;
        for (long time : downloadTimes) {
            sum += time;
        }
        return (double) (sum /1000.0) / downloadTimes.size();
    }

    /**
     * Calcula o desvio padrão dos tempos de download.
     * @return O desvio padrão.
     */
    public double getStandardDeviation() {
        if (downloadTimes.size() < 2) {
            return 0.0; // Desvio padrão não pode ser calculado com menos de 2 valores
        }
        double mean = getAverageTime();
        double sumOfSquaredDifferences = 0;
        for (long time : downloadTimes) {
            sumOfSquaredDifferences += Math.pow(time - mean, 2);
        }
        return Math.sqrt(sumOfSquaredDifferences / (downloadTimes.size() - 1)); // Usar n-1 para desvio padrão da amostra
    }
}