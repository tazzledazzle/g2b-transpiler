grammar Gradle;

// Parser rules
buildScript
    : (pluginsDeclaration | repositoriesDeclaration | dependenciesDeclaration | taskRegistration)* EOF
    ;

pluginsDeclaration
    : PLUGINS LBRACE (pluginEntry)* RBRACE
    ;

pluginEntry
    : ID STRING (VERSION STRING)*
    ;
repositoriesDeclaration
    : REPOSITORIES LBRACE (repositoryEntry)* RBRACE
    ;

repositoryEntry
    : MAVENCENTRAL
    | JCENTER
    | MAVEN LBRACE URL COLON STRING RBRACE
    ;


dependenciesDeclaration
    : DEPENDENCIES LBRACE (dependencyEntry)* RBRACE
    ;

dependencyEntry
    : IMPLEMENTATION STRING
    ;

taskRegistration
    : TASKS DOT REGISTER LPAREN STRING COMMA IDENTIFIER RPAREN LBRACE (taskStatement)* RBRACE
    ;

taskStatement
    : IDENTIFIER EQUALS STRING
    | IDENTIFIER EQUALS identifierPath
    ;

identifierPath
    : IDENTIFIER (DOT IDENTIFIER)*
    ;

// Lexer rules
PLUGINS          : 'plugins';
DEPENDENCIES     : 'dependencies';
REPOSITORIES     : 'repositories';
MAVENCENTRAL     : 'mavenCentral';
JCENTER          : 'jcenter' ;
MAVEN            : 'maven' ;
URL              : 'url' ;
TASKS            : 'tasks';
REGISTER         : 'register';
IMPLEMENTATION   : 'implementation';
VERSION          : 'version';
ID               : 'id';
LBRACE           : '{';
RBRACE           : '}';
LPAREN           : '(';
RPAREN           : ')';
DOT              : '.';
COMMA            : ',';
COLON            : ':';
EQUALS           : '=';
STRING
    : '\'' (~['\r\n])* '\''
    ;

IDENTIFIER
    : [a-zA-Z_] [a-zA-Z_0-9]*
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

COMMENT_BLOCK
    : '/*' .*? '*/' -> skip
    ;

COMMENT_LINE
    : '//' ~[\r\n]* -> skip
    ;
