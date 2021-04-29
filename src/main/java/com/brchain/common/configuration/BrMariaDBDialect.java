package com.brchain.common.configuration;

import org.hibernate.dialect.MariaDB103Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class BrMariaDBDialect extends MariaDB103Dialect {
	
	public BrMariaDBDialect() {
		super();

		this.registerFunction("group_concat", new StandardSQLFunction("group_concat", StandardBasicTypes.STRING));
	}

}
