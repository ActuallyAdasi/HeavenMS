package tools.parallelism;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import server.MapleItemInformationProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;

@AllArgsConstructor
@Log4j2
public class CacheMapleDataAction extends RecursiveAction {
    private final static int FORK_SIZE_THRESHOLD = 8;

    private final MapleDataProvider dataProvider;
    private final List<MapleDataDirectoryEntry> dataDirectories;
    // Should be concurrent hashmaps!
    private final ConcurrentHashMap<String, MapleItemInformationProvider.MapleDataFileAndDirName> filesByName;
    private final ConcurrentHashMap<String, MapleData> mapleDataCache;

    @Override
    protected void compute() {
        if (dataDirectories.isEmpty()) {
            return;
        }
        if (dataDirectories.size() <= FORK_SIZE_THRESHOLD) {
            for (final MapleDataDirectoryEntry dataDirectory : dataDirectories) {
                cacheMapleDataForDirectory(dataDirectory);
            }
        }
        int mid = dataDirectories.size() / 2;
        final List<MapleDataDirectoryEntry> dataDirs1 = dataDirectories.subList(0, mid);
        final List<MapleDataDirectoryEntry> dataDirs2 = dataDirectories.subList(mid, dataDirectories.size());
        final CacheMapleDataAction task1 = new CacheMapleDataAction(dataProvider, dataDirs1, filesByName, mapleDataCache);
        final CacheMapleDataAction task2 = new CacheMapleDataAction(dataProvider, dataDirs2, filesByName, mapleDataCache);
        task1.fork();
        task2.fork();
        task1.join();
        task2.join();
    }

    private void cacheMapleDataForDirectory(final MapleDataDirectoryEntry dataDirectory) {
        final List<MapleDataFileEntry> dataFiles = dataDirectory.getFiles();
        log.info("Initializing {} files in {} directory...",
                dataFiles.size(), dataDirectory.getName());
        for (final MapleDataFileEntry iFile : dataDirectory.getFiles()) {
            initializeFileAndLoadData(
                    dataProvider, filesByName, mapleDataCache, iFile, dataDirectory.getName());
        }
        log.info("Done initializing {} files in {} directory.",
                dataFiles.size(), dataDirectory.getName());
    }

    private void initializeFileAndLoadData(
            final MapleDataProvider dataToGet,
            final Map<String, MapleItemInformationProvider.MapleDataFileAndDirName> filesByName,
            final Map<String, MapleData> dataToLoad,
            final MapleDataFileEntry iFile,
            final String directoryName
    ) {
        final MapleItemInformationProvider.MapleDataFileAndDirName entry = new MapleItemInformationProvider.MapleDataFileAndDirName();
        entry.file = iFile;
        entry.dirName = directoryName;
        filesByName.put(iFile.getName(), entry);
        loadDataGreedy(dataToGet, dataToLoad, directoryName, iFile.getName());
    }

    private void loadDataGreedy(
            final MapleDataProvider dataToGet,
            final Map<String, MapleData> dataToWrite,
            final String directoryName,
            final String fileName
    ) {
        final String compositeKeyPrefix = String.format("%s/%s", directoryName, fileName);
        final MapleData fileData = dataToGet.getData(compositeKeyPrefix);

        // for equip, the structure is one level less deep. Check if fileData is a number to verify.
        if (fileData.getName() != null && fileData.getName().matches("\\d+")) {
            // only numbers here, grab the extra depth
            for (MapleData child : fileData.getChildren()) {
                final String compositeKey = String.format("%s:%s", compositeKeyPrefix, child.getName());
                dataToWrite.put(compositeKey, child);
            }
        } else {
            // Otherwise, not numbers, likely a leaf node. Populate dataByCompositeKey with this level of data.
            dataToWrite.put(compositeKeyPrefix, fileData);
        }
    }
}
