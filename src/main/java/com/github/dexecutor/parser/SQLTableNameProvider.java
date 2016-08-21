package com.github.dexecutor.parser;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dexecutor.support.TablesNamesFinder;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

public class SQLTableNameProvider implements TableNameProvider {

	private static final Logger logger = LoggerFactory.getLogger(SQLTableNameProvider.class);

	@Override
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

	@Override
	public List<String> provideTableNames(String instructions) {
		logger.debug("Going to parse : {} ", instructions);
		Statement statement = null;
		try {
			statement = CCJSqlParserUtil.parse(instructions);
		} catch (JSQLParserException e) {
			logger.error("error parsing sql : " + instructions, e);
			throw new RuntimeException(e.getMessage());
		}
		List<String> tables = new TablesNamesFinder().getTableList(statement);
		logger.debug("Table name extracted, SQL ( {} ): Tables ( {} )", instructions, tables);
		return tables;
	}
}
