package tools.parallelism;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import provider.MapleData;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import server.MapleItemInformationProvider;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;

@AllArgsConstructor
@Log4j2
public class FasterCacheMapleDataAction extends RecursiveAction {
    private final static int FORK_SIZE_THRESHOLD = 16;

    private final MapleDataProvider dataProvider;
    private final List<MapleDataFileEntry> dataFiles;
    // Should be concurrent hashmaps!
    private final ConcurrentHashMap<String, MapleItemInformationProvider.MapleDataFileAndDirName> filesByName;
    private final ConcurrentHashMap<String, MapleData> mapleDataCache;
    private final String directoryName;
    private final boolean isEquip;

    @Override
    protected void compute() {
        log.debug("Computing FasterCacheMapleDataAction for {} data files.", dataFiles.size());
        if (dataFiles.isEmpty()) {
            return;
        }
        if (dataFiles.size() <= FORK_SIZE_THRESHOLD) {
            for (final MapleDataFileEntry dataFile : dataFiles) {
                cacheMapleDataEntry(dataFile);
            }
            log.debug("Cached {} data files.", dataFiles.size());
            return;
        }
        int mid = dataFiles.size() / 2;
        final List<MapleDataFileEntry> dataFiles1 = dataFiles.subList(0, mid);
        final List<MapleDataFileEntry> dataFiles2 = dataFiles.subList(mid, dataFiles.size());
        final FasterCacheMapleDataAction task1 = new FasterCacheMapleDataAction(dataProvider, dataFiles1, filesByName, mapleDataCache, directoryName, isEquip);
        final FasterCacheMapleDataAction task2 = new FasterCacheMapleDataAction(dataProvider, dataFiles2, filesByName, mapleDataCache, directoryName, isEquip);
        task1.fork();
        task2.fork();
        task1.join();
        task2.join();
    }

    private void cacheMapleDataEntry(final MapleDataFileEntry iFile) {
        final MapleItemInformationProvider.MapleDataFileAndDirName entry = new MapleItemInformationProvider.MapleDataFileAndDirName();
        entry.file = iFile;
        entry.dirName = directoryName;
        filesByName.put(iFile.getName(), entry);
        final String compositeKeyPrefix = String.format("%s/%s", directoryName, iFile.getName());
        final MapleData fileData = dataProvider.getData(compositeKeyPrefix);
        // check for items that have one less level of depth
        if ((!isEquip && appropriateItemDepth(fileData)) || (isEquip && appropriateEquipDepth(fileData))) {
            // Appropriate depth already, put item with prefix
            mapleDataCache.put(compositeKeyPrefix, fileData);
        } else {
            // Need another level of depth, put children with full composite key
            for (final MapleData child : fileData.getChildren()) {
                final String compositeKey = String.format("%s:%s", compositeKeyPrefix, child.getName());
                mapleDataCache.put(compositeKey, child);
            }
        }
    }

    private boolean appropriateItemDepth(final MapleData fileData) {
        // If child[0].name == "info", it's the right level of item depth.
        final List<MapleData> children = fileData.getChildren();
        return children != null && !children.isEmpty() && "info".equals(children.get(0).getName());
    }

    private boolean appropriateEquipDepth(final MapleData fileData) {
        // If data.name isn't all numbers, it's the right level of equip depth.
        return fileData.getName() != null && !fileData.getName().matches("\\d+");
    }
}
