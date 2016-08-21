package com.github.dexecutor.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CompositeTableNameProvider implements TableNameProvider {

	private static final String REGEX_SEMI_COLON = ";";
	private static final String START_TAG_COMMENT = "--";
	private List<TableNameProvider> providers;

	public CompositeTableNameProvider(List<TableNameProvider> newProviders) {
		this.providers = newProviders;
	}

	@Override
	public boolean canProvideTableNames(String instructions) {
		return true;
	}

	@Override
	public List<String> provideTableNames(String instructions) {
		List<String> tableNames = new ArrayList<>();
		String[] partitioned = instructions.split(REGEX_SEMI_COLON);
		for (String instruction : partitioned) {
			if (isInstructionValid(instruction)) {
				List<String> tns = tableNames(instruction);
				if (isNotEmpty(tns)) {
					tableNames.addAll(tns);
				}
			}
		}
		return tableNames;
	}

	private List<String> tableNames(String instruction) {
		List<String> tableNames = new ArrayList<>();
		for (TableNameProvider provider : this.providers) {
			if (provider.canProvideTableNames(instruction)) {
				return provider.provideTableNames(instruction);				
			}
		}
		return tableNames;
	}

	private boolean isNotEmpty(Collection<String> collection) {
		return collection != null && !collection.isEmpty();
	}

	private boolean isInstructionValid(String instruction) {
		return isNotBlank(instruction) && isNotComment(instruction);
	}

	private boolean isNotComment(String instruction) {
		return !instruction.startsWith(START_TAG_COMMENT);
	}

	private boolean isNotBlank(String instruction) {
		return instruction != null && !instruction.isEmpty();
	}
}
