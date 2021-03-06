package cc.blynk.server.core.reporting.raw;

import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.reporting.GraphPinRequest;
import cc.blynk.utils.FileUtils;
import cc.blynk.utils.structure.LimitedArrayDeque;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Raw data storage for graph LIVE stream.
 *
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 25.01.17.
 */
public class RawDataCacheForGraphProcessor {

    private static final int GRAPH_CACHE_SIZE = 60;

    private final Map<BaseReportingKey, LimitedArrayDeque<GraphValue>> rawStorage;

    public RawDataCacheForGraphProcessor() {
        rawStorage = new ConcurrentHashMap<>();
    }

    public void collect(BaseReportingKey baseReportingKey, GraphValue graphCacheValue) {
        LimitedArrayDeque<GraphValue> cache = rawStorage.get(baseReportingKey);
        if (cache == null) {
            cache = new LimitedArrayDeque<>(GRAPH_CACHE_SIZE);
            rawStorage.put(baseReportingKey, cache);
        }
        cache.add(graphCacheValue);
    }

    public ByteBuffer getLiveGraphData(User user, GraphPinRequest graphPinRequest) {
        LimitedArrayDeque<GraphValue> cache = rawStorage.get(new BaseReportingKey(user, graphPinRequest));

        if (cache != null) {
            return toByteBuffer(cache);
        }

        return null;
    }

    private ByteBuffer toByteBuffer(LimitedArrayDeque<GraphValue> cache) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(cache.size() * FileUtils.SIZE_OF_REPORT_ENTRY);
        for (GraphValue graphValue : cache) {
            byteBuffer.putDouble(graphValue.value)
                    .putLong(graphValue.ts);
        }
        return byteBuffer;
    }
}
