package com.github.dexecutor.migration.parser;

import java.util.List;

public interface TableNameProvider {
	boolean canProvideTableNames(String instructions);
	List<String> provideTableNames(String instructions);
}
