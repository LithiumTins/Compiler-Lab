package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SemanticAnalyzer implements ActionObserver {

    private SymbolTable symbolTable;
    private final ArrayList<Token> tokenStack = new ArrayList<>();
    private final ArrayList<SourceCodeType> dataStack = new ArrayList<>();

    @Override
    public void whenAccept(Status currentStatus) {
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        switch (production.index()) {
            case 4 -> {
                symbolTable.get(tokenStack.getLast().getText()).setType(dataStack.get(dataStack.size() - 2));
            }
            case 5 -> {
                tokenStack.removeLast();
                dataStack.removeLast();
                tokenStack.add(null);
                dataStack.add(SourceCodeType.Int);
            }
            default -> {
                for (int i = 0; i < production.body().size(); i++) {
                    tokenStack.removeLast();
                    dataStack.removeLast();
                }
                tokenStack.add(null);
                dataStack.add(null);
            }
        }
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        tokenStack.add(currentToken);
        if (currentToken.getKind().getTermName().equals("int"))
            dataStack.add(SourceCodeType.Int);
        else
            dataStack.add(null);
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
        symbolTable = table;
    }
}

