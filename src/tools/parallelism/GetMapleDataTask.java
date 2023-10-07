package tools.parallelism;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import server.MapleItemInformationProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

@AllArgsConstructor
@Log4j2
public class GetMapleDataTask extends RecursiveTask<Map<String, MapleData>> {

    private final MapleDataProvider dataProvider;
    private final List<MapleDataDirectoryEntry> dataDirectories;
    private final Map<String, MapleItemInformationProvider.MapleDataFileAndDirName> filesByName;

    @Override
    protected Map<String, MapleData> compute() {
        if (dataDirectories.isEmpty()) {
            return new HashMap<>();
        }
        if (dataDirectories.size() == 1) {
            return getMapleDataForDirectory(dataDirectories.get(0));
        }
        final Map<String, MapleData> dataToReturn = new HashMap<>();
        int mid = dataDirectories.size() / 2;
        final List<MapleDataDirectoryEntry> dataDirs1 = dataDirectories.subList(0, mid);
        final List<MapleDataDirectoryEntry> dataDirs2 = dataDirectories.subList(mid, dataDirectories.size());
        final GetMapleDataTask task1 = new GetMapleDataTask(dataProvider, dataDirs1, filesByName);
        final GetMapleDataTask task2 = new GetMapleDataTask(dataProvider, dataDirs2, filesByName);
        task1.fork();
        dataToReturn.putAll(task2.compute());
        dataToReturn.putAll(task1.join());
        return dataToReturn;
    }

    private Map<String, MapleData> getMapleDataForDirectory(
            final MapleDataDirectoryEntry dataDirectory
    ) {
        final Map<String, MapleData> dataToReturn = new HashMap<>();
        initializeDataDirectory(dataProvider, filesByName, dataToReturn, dataDirectory);
        return dataToReturn;
    }

    private void initializeDataDirectory(
            final MapleDataProvider dataToGet,
            final Map<String, MapleItemInformationProvider.MapleDataFileAndDirName> filesByName,
            final Map<String, MapleData> dataToLoad,
            final MapleDataDirectoryEntry dataDirectory
    ) {
        final List<MapleDataFileEntry> dataFiles = dataDirectory.getFiles();
        log.info("Initializing {} files in {} directory...",
                dataFiles.size(), dataDirectory.getName());
        for (final MapleDataFileEntry iFile : dataDirectory.getFiles()) {
            initializeFileAndLoadData(
                    dataToGet, filesByName, dataToLoad, iFile, dataDirectory.getName());
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
