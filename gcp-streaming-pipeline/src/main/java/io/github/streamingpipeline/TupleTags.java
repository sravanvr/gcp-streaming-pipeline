package io.github.streamingpipeline;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;
import com.google.bigtable.v2.Mutation;
import com.google.protobuf.ByteString;
import static io.github.streamingpipeline.utils.Constants.*;

public class TupleTags {

	public final static TupleTag<KV<ByteString, Iterable<Mutation>>> accountTag = new TupleTag<KV<ByteString, Iterable<Mutation>>>() {
		private static final long serialVersionUID = 1L;
	};
	
	public final static TupleTag<KV<ByteString, Iterable<Mutation>>> accountLookUpTag = new TupleTag<KV<ByteString, Iterable<Mutation>>>() {
		private static final long serialVersionUID = 1L;
	};
	
	public static Map<String, TupleTag<KV<ByteString, Iterable<Mutation>>>> getTupleTagsMap() {
		Map<String, TupleTag<KV<ByteString, Iterable<Mutation>>>> tupleTagsMap = new LinkedHashMap<>();
		tupleTagsMap.put(ACCOUNT_TAG, accountTag);
		tupleTagsMap.put(ACCOUNT_LOOKUP, accountLookUpTag);
		return tupleTagsMap;
	}

	public static List<TupleTag<?>> getTupleTagList() {
		List<TupleTag<?>> list = new ArrayList<>(getTupleTagsMap().values());
		list.remove(0);
		return list;
	}
}