package com.github.dexecutor.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mnadeem.TableNameParser;

public class SQLTableNameProvider implements TableNameProvider {

	private static final Logger logger = LoggerFactory.getLogger(SQLTableNameProvider.class);

	public boolean canProvideTableNames(String instructions) {
		return isSQLStatement(instructions);
	}

	private boolean isSQLStatement(String instructions) {
		return instructions.startsWith("SELECT ")
				|| instructions.startsWith("UPDATE ")
				|| instructions.startsWith("DELETE ")
				|| instructions.startsWith("DROP ")
				|| instructions.startsWith("MERGE ")
				|| instructions.startsWith("INSERT ")
				|| instructions.startsWith("CREATE ");
	}

	public List<String> provideTableNames(String instructions) {
		logger.debug("Going to parse : {} ", instructions);

		Collection<String> tables = new TableNameParser(instructions).tables();
		logger.debug("Table name extracted, SQL ( {} ): Tables ( {} )", instructions, tables);
		return new ArrayList<String>(tables);
	}
}
