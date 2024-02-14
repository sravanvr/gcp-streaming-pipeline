package io.github.streamingpipeline.service;

import static io.github.streamingpipeline.utils.Constants.ACCOUNT_COLUMN_QUALIFIER;
import static io.github.streamingpipeline.utils.Constants.COLUMN_FAMILY;
import static io.github.streamingpipeline.utils.Constants.LOOKUP_COLUMN_FAMILY;
import static io.github.streamingpipeline.utils.Constants.LOOKUP_COLUMN_QUALIFIER;
import static io.github.streamingpipeline.utils.Constants.TAG_COLUMN_QUALIFIER;
import static io.github.streamingpipeline.utils.Constants.UTF8;

import java.nio.charset.Charset;

import io.github.streamingpipeline.model.UberAccount;
import org.apache.beam.sdk.values.KV;

import com.google.bigtable.v2.Mutation;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;

/***
 * 
 * @author Sravan Vedala
 *
 */
public class BigtableUtil {

	public static ByteString toByteString(byte[] byteBuffer) {
		return ByteString.copyFrom(byteBuffer);
	}

	public static KV<ByteString, Iterable<Mutation>> createLookupRow(String lookupRowKey, String lookupRowValue) {
		ImmutableList.Builder<Mutation> mutations = ImmutableList.builder();
		createLookupRowCell(mutations, LOOKUP_COLUMN_FAMILY, LOOKUP_COLUMN_QUALIFIER, lookupRowValue.getBytes());
		return KV.of(ByteString.copyFromUtf8(lookupRowKey), mutations.build());
	}

	public static void createLookupRowCell(ImmutableList.Builder<Mutation> mutations, String colFamily,
			String accountColQualifier, byte[] lookupColValueJson) {
		long currentMicros = System.currentTimeMillis() * 1000;
		Mutation.SetCell setCell1 = Mutation.SetCell.newBuilder().setFamilyName(colFamily)
				.setColumnQualifier(toByteString(accountColQualifier.getBytes(Charset.forName(UTF8))))
				.setTimestampMicros(currentMicros).setValue(toByteString(lookupColValueJson)).build();
		mutations.add(Mutation.newBuilder().setSetCell(setCell1).build());
	}
	
	public static KV<ByteString, Iterable<Mutation>> createAccountRow(String accountString, String tags, String rowKey) {
		ImmutableList.Builder<Mutation> mutations = ImmutableList.builder();
		createAccountRowCell(mutations, COLUMN_FAMILY, ACCOUNT_COLUMN_QUALIFIER, TAG_COLUMN_QUALIFIER,
				accountString.getBytes(), tags.getBytes());
		return KV.of(ByteString.copyFromUtf8(rowKey), mutations.build());
	}
	
	public static void createAccountRowCell(ImmutableList.Builder<Mutation> mutations, String colFamily,
			String accountColQualifier, String tagsColQualifier, byte[] accountValueJson, byte[] tagsValueJson
			) {
		long currentMicros = System.currentTimeMillis() * 1000;
		Mutation.SetCell accountCell = Mutation.SetCell.newBuilder().setFamilyName(colFamily)
				.setColumnQualifier(toByteString(accountColQualifier.getBytes(Charset.forName(UTF8))))
				.setTimestampMicros(currentMicros).setValue(toByteString(accountValueJson)).build();

		Mutation.SetCell tagsCell = Mutation.SetCell.newBuilder().setFamilyName(colFamily)
				.setColumnQualifier(toByteString(tagsColQualifier.getBytes(Charset.forName(UTF8))))
				.setTimestampMicros(currentMicros).setValue(toByteString(tagsValueJson)).build();

		mutations.add(Mutation.newBuilder().setSetCell(accountCell).build());
		mutations.add(Mutation.newBuilder().setSetCell(tagsCell).build());
	}

	/***
	 * 
	 * Builds Accounts row-key.
	 * 
	 */
	public static String buildAccountRowKey(UberAccount account) {
		return account.getUberCustomerId() + "#aid#" + account.getId();
	}
}
