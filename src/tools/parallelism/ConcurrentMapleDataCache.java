package tools.parallelism;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import server.MapleItemInformationProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

@Log4j2
public class ConcurrentMapleDataCache {
    private final static int FORK_SIZE_THRESHOLD = 16;
    private boolean isCacheWarmed = false;

    private final static ConcurrentHashMap<String, MapleItemInformationProvider.MapleDataFileAndDirName> filesByName = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, MapleData> mapleDataCache = new ConcurrentHashMap<>();

    private final MapleDataProvider dataProvider;
    private final boolean isEquip;

    public ConcurrentMapleDataCache(final MapleDataProvider dataProvider, final boolean isEquip) {
        this.dataProvider = dataProvider;
        this.isEquip = isEquip;
    }

    public List<MapleData> getPossibleDataForItemId(final int itemId) {
        final List<String> fileNamesToCheck = getFileNamesToCheck(itemId);
        final List<MapleData> possibleData = new ArrayList<>();
        String keyUsed = "";
        for (final String fileName : fileNamesToCheck) {
            if (filesByName.containsKey(fileName)) {
                final MapleItemInformationProvider.MapleDataFileAndDirName entry = filesByName.get(fileName);
                final String compositeKeyPrefix = entry.dirName + "/" + fileName;
                final MapleData prefixData = dataProvider.getData(compositeKeyPrefix);
                if (prefixData != null) {
                    possibleData.add(prefixData);
                    keyUsed = compositeKeyPrefix;
                }
                for (final String compositeKeySuffix : fileNamesToCheck) {
                    final String compositeKey = String.format("%s:%s", compositeKeyPrefix, compositeKeySuffix);
                    final MapleData compositeData = dataProvider.getData(compositeKey);
                    if (compositeData != null) {
                        possibleData.add(compositeData);
                        keyUsed = compositeKey;
                    }
                }
            }
        }
        if (possibleData.size() == 1) {
            log.info("found exactly 1 possible item for itemId {}: {}, adding to cache with key {}.", itemId, possibleData.get(0), keyUsed);
            mapleDataCache.put(keyUsed, possibleData.get(0));
        }
        if (possibleData.size() != 1) {
            log.warn("Not exactly one possible file for item id {}! Possible data: {}", itemId, possibleData);
        }
        return possibleData;
    }

    public List<MapleData> getPossibleDataFromCacheForItemId(final int itemId) {
        // Only use this if getItemData doesn't work for itemId
        // Then if this doesn't work, use getPossibleDataForItemId (slow, adds to cache)
        final List<String> fileNamesToCheck = getFileNamesToCheck(itemId);
        final List<MapleData> possibleData = new ArrayList<>();
        for (final String fileName : fileNamesToCheck) {
            if (filesByName.containsKey(fileName)) {
                final MapleItemInformationProvider.MapleDataFileAndDirName entry = filesByName.get(fileName);
                final String compositeKeyPrefix = entry.dirName + "/" + fileName;
                if (mapleDataCache.contains(compositeKeyPrefix)) {
                    possibleData.add(mapleDataCache.get(compositeKeyPrefix));
                }
                for (final String compositeKeySuffix : fileNamesToCheck) {
                    final String compositeKey = String.format("%s:%s", compositeKeyPrefix, compositeKeySuffix);
                    if (mapleDataCache.contains(compositeKey)) {
                        possibleData.add(mapleDataCache.get(compositeKey));
                    }
                }
            }
        }
        if (possibleData.size() == 1) {
            log.info("found exactly 1 possible item for itemId in cache {}: {}.", itemId, possibleData.get(0));
        }
        if (possibleData.size() != 1) {
            log.warn("Not exactly one possible file for item id {} in cache! Possible data: {}", itemId, possibleData);
        }
        return possibleData;
    }

    private static List<String> getFileNamesToCheck(int itemId) {
        final String itemIdString = String.format("%s", itemId);
        final String zeroPrefixedItemId = String.format("0%s", itemId);
        final String abbreviatedImageId = String.format("0%s.img", itemIdString.substring(0, 3));
        final String fullImageId = String.format("%s.img", itemIdString);
        final String equipImageId = String.format("0%s.img", itemIdString);
        return Arrays.asList(zeroPrefixedItemId, abbreviatedImageId, fullImageId, equipImageId);
    }

    public MapleItemInformationProvider.MapleDataFileAndDirName getFileByName(final String fileName) {
        return filesByName.get(fileName);
    }

    public MapleData getItemData(final int itemId) {
        final String itemIdString = String.format("%s", itemId);
        final String zeroPrefixedItemId = String.format("0%s", itemId);
        final String abbreviatedImageId = String.format("0%s.img", itemIdString.substring(0, 3));
        final String fullImageId = String.format("%s.img", itemIdString);
        final String equipImageId = String.format("0%s.img", itemIdString);

        // TODO: check logs for duplicates. if no duplicates, we can consolidate caches? Maybe...
        List<MapleData> possibleData = new ArrayList<>();

        // check abbreviated
        final MapleItemInformationProvider.MapleDataFileAndDirName abbrevFileAndDirName =
                filesByName.get(abbreviatedImageId);
        if (abbrevFileAndDirName != null) {
            final String abbrevCompositeKeyPrefix = abbrevFileAndDirName.dirName + "/" + abbreviatedImageId;
            final String abbrevCompositeKey = String.format("%s:%s", abbrevCompositeKeyPrefix, zeroPrefixedItemId);
            if (mapleDataCache.contains(abbrevCompositeKeyPrefix)) {
                possibleData.add(mapleDataCache.get(abbrevCompositeKeyPrefix));
            }
            if (mapleDataCache.contains(abbrevCompositeKey)) {
                possibleData.add(mapleDataCache.get(abbrevCompositeKey));
            }
        }

        // check equip style - only composite key prefix
        final MapleItemInformationProvider.MapleDataFileAndDirName equipFileAndDirName =
                filesByName.get(equipImageId);
        if (equipFileAndDirName != null) {
            final String equipCompositeKeyPrefix = equipFileAndDirName.dirName + "/" + equipImageId;
            if (mapleDataCache.contains(equipCompositeKeyPrefix)) {
                possibleData.add(mapleDataCache.get(equipCompositeKeyPrefix));
            }
        }

        // check full image - only composite key prefix?? that's what's currently in MapleItemInfo provider
        final MapleItemInformationProvider.MapleDataFileAndDirName fullItemFileAndDirName =
                filesByName.get(fullImageId);
        if (fullItemFileAndDirName != null) {
            final String fullItemCompositeKeyPrefix = fullItemFileAndDirName.dirName + "/" + fullImageId;
            if (mapleDataCache.contains(fullItemCompositeKeyPrefix)) {
                possibleData.add(mapleDataCache.get(fullItemCompositeKeyPrefix));
            }
        }
        if (possibleData.size() == 1) {
            return possibleData.get(0);
        }
        if (possibleData.size() > 1) {
            log.warn("MapleDataCache getItem found more than one for itemId {}: {}", itemId, possibleData);
            return possibleData.get(0);
        }
        return null;
    }

    public MapleData getItemData(final String cacheKey) {
        return mapleDataCache.get(cacheKey);
    }

    public void warmDataCache() {
        if (isCacheWarmed) {
            return;
        }
        final ForkJoinPool commonPool = ForkJoinPool.commonPool();
        int count = 0;
        final List<MapleDataDirectoryEntry> dataDirectories = dataProvider.getRoot().getSubdirectories();
        for (final MapleDataDirectoryEntry dir : dataDirectories) {
            count++;
            final CopyOnWriteArrayList<MapleDataFileEntry> dataFiles = new CopyOnWriteArrayList<>(dir.getFiles());
            final WarmCacheRecursiveAction cacheDataAction = new WarmCacheRecursiveAction(dataFiles, dir.getName(), 0, dataFiles.size() - 1);
            final String directoryType = isEquip ? "equip" : "item";
            log.info("Initializing {} files in {} directory {}/{}...",
                    dataFiles.size(), directoryType, count, dataDirectories.size());
            commonPool.invoke(cacheDataAction);
        }
        isCacheWarmed = true;
    }

    @AllArgsConstructor
    private class WarmCacheRecursiveAction extends RecursiveAction {
        final CopyOnWriteArrayList<MapleDataFileEntry> dataFiles;
        final String directoryName;
        private final int startIndex;
        private final int endIndex;
        @Override
        protected void compute() {
            final int numberItems = endIndex + 1 - startIndex;
            if (numberItems < 1) {
                return;
            }
            if (numberItems <= FORK_SIZE_THRESHOLD) {
                for (int i = startIndex; i <= endIndex; i++) {
                    final MapleDataFileEntry dataFile = dataFiles.get(i);
                    cacheMapleDataEntry(dataFile, directoryName);
                }
                log.debug("Cached {} data files.", numberItems);
                return;
            }
            int mid = numberItems / 2;
            int start2 = startIndex + mid;
            final WarmCacheRecursiveAction task1 = new WarmCacheRecursiveAction(dataFiles, directoryName, startIndex, start2 - 1);
            final WarmCacheRecursiveAction task2 = new WarmCacheRecursiveAction(dataFiles, directoryName, start2, endIndex);
            task1.fork();
            task2.fork();
            task1.join();
            task2.join();
        }
    }

    private void cacheMapleDataEntry(final MapleDataFileEntry iFile, final String directoryName) {
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

    private static boolean appropriateItemDepth(final MapleData fileData) {
        // If child[0].name == "info", it's the right level of item depth.
        final List<MapleData> children = fileData.getChildren();
        return children != null && !children.isEmpty() && "info".equals(children.get(0).getName());
    }

    private static boolean appropriateEquipDepth(final MapleData fileData) {
        // If data.name isn't all numbers, it's the right level of equip depth.
        return fileData.getName() != null && !fileData.getName().matches("\\d+");
    }
}
