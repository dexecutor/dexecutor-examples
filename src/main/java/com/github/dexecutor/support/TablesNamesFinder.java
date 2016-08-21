package com.github.dexecutor.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.WithinGroupExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class TablesNamesFinder implements SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor,
		SelectItemVisitor, StatementVisitor {

	private static final String NOT_SUPPORTED_YET = "Not supported yet.";
	private List<String> tables;
	/**
	 * There are special names, that are not table names but are parsed as
	 * tables. These names are collected here and are not included in the tables
	 * - names anymore.
	 */
	private List<String> otherItemNames;
	
	
	public List<String> getTableList(Statement statement) {
		
		if (statement instanceof Alter) {
			return getTableList((Alter)statement);
		} else if (statement instanceof CreateIndex) {
			return getTableList((CreateIndex)statement);
		} else if (statement instanceof CreateTable) {
			return getTableList((CreateTable)statement);
		} else if (statement instanceof CreateView) {
			return getTableList((CreateView)statement);
		} else if (statement instanceof Delete) {
			return getTableList((Delete)statement);
		} else if (statement instanceof Drop) {
			return getTableList((Drop)statement);
		} else if (statement instanceof Execute) {
			return getTableList((Execute)statement);
		} else if (statement instanceof Insert) {
			return getTableList((Insert)statement);
		} else if (statement instanceof Merge) {
			return getTableList((Merge)statement);
		} else if (statement instanceof Replace) {
			return getTableList((Merge)statement);
		} else if (statement instanceof Replace) {
			return getTableList((Merge)statement);
		} else if (statement instanceof Select) {
			return getTableList((Select)statement);
		} else if (statement instanceof Truncate) {
			return getTableList((Truncate)statement);
		} else if (statement instanceof Update) {
			return getTableList((Update)statement);
		}		
		return Collections.emptyList();
	}

	/**
	 * Main entry for this Tool class. A list of found tables is returned.
	 *
	 * @param delete
	 * @return
	 */
	public List<String> getTableList(Delete delete) {
		init();
		delete.accept(this);
		return tables;
	}

	/**
	 * Main entry for this Tool class. A list of found tables is returned.
	 *
	 * @param insert
	 * @return
	 */
	public List<String> getTableList(Insert insert) {
		init();
		insert.accept(this);
		return tables;
	}

	/**
	 * Main entry for this Tool class. A list of found tables is returned.
	 *
	 * @param replace
	 * @return
	 */
	public List<String> getTableList(Replace replace) {
		init();
		replace.accept(this);
		return tables;
	}

	/**
	 * Main entry for this Tool class. A list of found tables is returned.
	 *
	 * @param select
	 * @return
	 */
	public List<String> getTableList(Select select) {
		init();
		select.accept(this);
		return tables;
	}

	@Override
	public void visit(Select select) {
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		select.getSelectBody().accept(this);
	}

	/**
	 * Main entry for this Tool class. A list of found tables is returned.
	 *
	 * @param update
	 * @return
	 */
	public List<String> getTableList(Update update) {
		init();
		update.accept(this);
		return tables;
	}

	public List<String> getTableList(CreateTable create) {
		init();
		create.accept(this);
		return tables;
	}

	public List<String> getTableList(Expression expr) {
		init();
		expr.accept(this);
		return tables;
	}

	@Override
	public void visit(WithItem withItem) {
		otherItemNames.add(withItem.getName().toLowerCase());
		withItem.getSelectBody().accept(this);
	}

	@Override
	public void visit(PlainSelect plainSelect) {
		if (plainSelect.getSelectItems() != null) {
			for (SelectItem item : plainSelect.getSelectItems()) {
				item.accept(this);
			}
		}

		if (plainSelect.getFromItem() != null) {
			plainSelect.getFromItem().accept(this);
		}

		if (plainSelect.getJoins() != null) {
			for (Join join : plainSelect.getJoins()) {
				join.getRightItem().accept(this);
			}
		}
		if (plainSelect.getWhere() != null) {
			plainSelect.getWhere().accept(this);
		}
		if (plainSelect.getOracleHierarchical() != null) {
			plainSelect.getOracleHierarchical().accept(this);
		}
	}

	@Override
	public void visit(Table tableName) {
		String tableWholeName = tableName.getFullyQualifiedName();
		if (!otherItemNames.contains(tableWholeName.toLowerCase()) && !tables.contains(tableWholeName)) {
			tables.add(tableWholeName);
		}
	}

	@Override
	public void visit(SubSelect subSelect) {
		subSelect.getSelectBody().accept(this);
	}

	@Override
	public void visit(Addition addition) {
		visitBinaryExpression(addition);
	}

	@Override
	public void visit(AndExpression andExpression) {
		visitBinaryExpression(andExpression);
	}

	@Override
	public void visit(Between between) {
		between.getLeftExpression().accept(this);
		between.getBetweenExpressionStart().accept(this);
		between.getBetweenExpressionEnd().accept(this);
	}

	@Override
	public void visit(Column tableColumn) {
	}

	@Override
	public void visit(Division division) {
		visitBinaryExpression(division);
	}

	@Override
	public void visit(DoubleValue doubleValue) {
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		visitBinaryExpression(equalsTo);
	}

	@Override
	public void visit(Function function) {
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		visitBinaryExpression(greaterThan);
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		visitBinaryExpression(greaterThanEquals);
	}

	@Override
	public void visit(InExpression inExpression) {
		inExpression.getLeftExpression().accept(this);
		inExpression.getRightItemsList().accept(this);
	}

	@Override
	public void visit(SignedExpression signedExpression) {
		signedExpression.getExpression().accept(this);
	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
	}

	@Override
	public void visit(LikeExpression likeExpression) {
		visitBinaryExpression(likeExpression);
	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		existsExpression.getRightExpression().accept(this);
	}

	@Override
	public void visit(LongValue longValue) {
	}

	@Override
	public void visit(MinorThan minorThan) {
		visitBinaryExpression(minorThan);
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		visitBinaryExpression(minorThanEquals);
	}

	@Override
	public void visit(Multiplication multiplication) {
		visitBinaryExpression(multiplication);
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		visitBinaryExpression(notEqualsTo);
	}

	@Override
	public void visit(NullValue nullValue) {
	}

	@Override
	public void visit(OrExpression orExpression) {
		visitBinaryExpression(orExpression);
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		parenthesis.getExpression().accept(this);
	}

	@Override
	public void visit(StringValue stringValue) {
	}

	@Override
	public void visit(Subtraction subtraction) {
		visitBinaryExpression(subtraction);
	}

	public void visitBinaryExpression(BinaryExpression binaryExpression) {
		binaryExpression.getLeftExpression().accept(this);
		binaryExpression.getRightExpression().accept(this);
	}

	@Override
	public void visit(ExpressionList expressionList) {
		for (Expression expression : expressionList.getExpressions()) {
			expression.accept(this);
		}

	}

	@Override
	public void visit(DateValue dateValue) {
	}

	@Override
	public void visit(TimestampValue timestampValue) {
	}

	@Override
	public void visit(TimeValue timeValue) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.CaseExpression)
	 */
	@Override
	public void visit(CaseExpression caseExpression) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.WhenClause)
	 */
	@Override
	public void visit(WhenClause whenClause) {
	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		allComparisonExpression.getSubSelect().getSelectBody().accept(this);
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		anyComparisonExpression.getSubSelect().getSelectBody().accept(this);
	}

	@Override
	public void visit(SubJoin subjoin) {
		subjoin.getLeft().accept(this);
		subjoin.getJoin().getRightItem().accept(this);
	}

	@Override
	public void visit(Concat concat) {
		visitBinaryExpression(concat);
	}

	@Override
	public void visit(Matches matches) {
		visitBinaryExpression(matches);
	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		visitBinaryExpression(bitwiseAnd);
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		visitBinaryExpression(bitwiseOr);
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		visitBinaryExpression(bitwiseXor);
	}

	@Override
	public void visit(CastExpression cast) {
		cast.getLeftExpression().accept(this);
	}

	@Override
	public void visit(Modulo modulo) {
		visitBinaryExpression(modulo);
	}

	@Override
	public void visit(AnalyticExpression analytic) {
	}

	@Override
	public void visit(SetOperationList list) {
		for (SelectBody plainSelect : list.getSelects()) {
			plainSelect.accept(this);
		}
	}

	@Override
	public void visit(ExtractExpression eexpr) {
	}

	@Override
	public void visit(LateralSubSelect lateralSubSelect) {
		lateralSubSelect.getSubSelect().getSelectBody().accept(this);
	}

	@Override
	public void visit(MultiExpressionList multiExprList) {
		for (ExpressionList exprList : multiExprList.getExprList()) {
			exprList.accept(this);
		}
	}

	@Override
	public void visit(ValuesList valuesList) {
	}

	/**
	 * Initializes table names collector.
	 */
	protected void init() {
		otherItemNames = new ArrayList<String>();
		tables = new ArrayList<String>();
	}

	@Override
	public void visit(IntervalExpression iexpr) {
	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {
	}

	@Override
	public void visit(OracleHierarchicalExpression oexpr) {
		if (oexpr.getStartExpression() != null) {
			oexpr.getStartExpression().accept(this);
		}

		if (oexpr.getConnectExpression() != null) {
			oexpr.getConnectExpression().accept(this);
		}
	}

	@Override
	public void visit(RegExpMatchOperator rexpr) {
		visitBinaryExpression(rexpr);
	}

	@Override
	public void visit(RegExpMySQLOperator rexpr) {
		visitBinaryExpression(rexpr);
	}

	@Override
	public void visit(JsonExpression jsonExpr) {
	}

	@Override
	public void visit(AllColumns allColumns) {
	}

	@Override
	public void visit(AllTableColumns allTableColumns) {
	}

	@Override
	public void visit(SelectExpressionItem item) {
		item.getExpression().accept(this);
	}

	@Override
	public void visit(WithinGroupExpression wgexpr) {
	}

	@Override
	public void visit(UserVariable var) {
	}

	@Override
	public void visit(NumericBind bind) {

	}

	@Override
	public void visit(KeepExpression aexpr) {
	}

	@Override
	public void visit(MySQLGroupConcat groupConcat) {
	}

	@Override
	public void visit(Delete delete) {
		tables.add(delete.getTable().getName());
		if (delete.getWhere() != null) {
			delete.getWhere().accept(this);
		}
	}

	@Override
	public void visit(Update update) {
		for (Table table : update.getTables()) {
			tables.add(table.getName());
		}
		if (update.getExpressions() != null) {
			for (Expression expression : update.getExpressions()) {
				expression.accept(this);
			}
		}

		if (update.getFromItem() != null) {
			update.getFromItem().accept(this);
		}

		if (update.getJoins() != null) {
			for (Join join : update.getJoins()) {
				join.getRightItem().accept(this);
			}
		}

		if (update.getWhere() != null) {
			update.getWhere().accept(this);
		}
	}

	@Override
	public void visit(Insert insert) {
		tables.add(insert.getTable().getName());
		if (insert.getItemsList() != null) {
			insert.getItemsList().accept(this);
		}
		if (insert.getSelect() != null) {
			visit(insert.getSelect());
		}
	}

	@Override
	public void visit(Replace replace) {
		tables.add(replace.getTable().getName());
		if (replace.getExpressions() != null) {
			for (Expression expression : replace.getExpressions()) {
				expression.accept(this);
			}
		}
		if (replace.getItemsList() != null) {
			replace.getItemsList().accept(this);
		}
	}

	@Override
	public void visit(Drop drop) {
		tables.add(drop.getName().getName());
	}

	@Override
	public void visit(Truncate truncate) {
		throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
	}

	@Override
	public void visit(CreateIndex createIndex) {
		throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
	}

	@Override
	public void visit(CreateTable create) {
		tables.add(create.getTable().getFullyQualifiedName());
		if (create.getSelect() != null) {
			create.getSelect().accept(this);
		}
	}

	@Override
	public void visit(CreateView createView) {
		throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
	}

	@Override
	public void visit(Alter alter) {
		throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
	}

	@Override
	public void visit(Statements stmts) {
		throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
	}

	@Override
	public void visit(Execute execute) {
		throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
	}

	@Override
	public void visit(SetStatement set) {
		throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
	}

	@Override
	public void visit(RowConstructor rowConstructor) {
		for (Expression expr : rowConstructor.getExprList().getExpressions()) {
			expr.accept(this);
		}
	}

	@Override
	public void visit(HexValue hexValue) {

	}

	@Override
	public void visit(Merge merge) {
		tables.add(merge.getTable().getName());
	}

	@Override
	public void visit(OracleHint hint) {
	}

	@Override
	public void visit(TableFunction valuesList) {
	}

	public List<String> getTableList(Merge merge) {
		init();
		merge.accept(this);
		if (merge.getUsingTable() != null) {
			merge.getUsingTable().accept(this);
		} else if (merge.getUsingSelect() != null) {
			merge.getUsingSelect().accept((FromItemVisitor) this);
		}

		return tables;
	}

	public List<String> getTableList(Drop drop) {
		init();
		drop.accept(this);
		return tables;
	}

	@Override
	public void visit(AlterView alterView) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimeKeyExpression timeKeyExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DateTimeLiteralExpression literal) {
		// TODO Auto-generated method stub
		
	}
}