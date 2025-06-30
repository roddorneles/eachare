import java.util.*;

public class DownloadKey {
    private int chunkSize;
    private int numberOfPeers;
    private long fileSize; // Usar long para o tamanho do arquivo, pois pode ser grande

    public DownloadKey(int chunkSize, int numberOfPeers, long fileSize) {
        this.chunkSize = chunkSize;
        this.numberOfPeers = numberOfPeers;
        this.fileSize = fileSize;
    }

    // Gerar métodos getters e setters
    public int getChunkSize() {
        return chunkSize;
    }

    public int getNumberOfPeers() {
        return numberOfPeers;
    }

    public long getFileSize() {
        return fileSize;
    }

    // É crucial sobrescrever os métodos equals() e hashCode() para que a chave funcione corretamente em um HashMap.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DownloadKey that = (DownloadKey) o;
        return chunkSize == that.chunkSize &&
               numberOfPeers == that.numberOfPeers &&
               fileSize == that.fileSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunkSize, numberOfPeers, fileSize);
    }
}