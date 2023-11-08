package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.*;
import java.util.*;
import java.util.stream.StreamSupport;

/**
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {

    private final SymbolTable symbolTable;
    private final Deque<Integer> deque = new LinkedList<>();
    private final ArrayList<Token> tokens = new ArrayList<>();

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            int character;
            while ((character = reader.read()) != -1) {
                deque.addLast(character);
            }
            deque.addLast(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        // 定义状态的枚举类型
        enum Status {
            START, SPACE,
            I, IN, INT, _INT,
            R, RE, RET, RETU, RETUR, RETURN, _RETURN,
            _OPERATOR_ASSIGN,
            _OPERATOR_COMMA,
            _SEMICOLON,
            _OPERATOR_ADD,
            _OPERATOR_SUB,
            _OPERATOR_MUL,
            _OPERATOR_DIV,
            _PARENTHESIS_LEFT,
            _PARENTHESIS_RIGHT,
            LETTER, _ID,
            DIGIT, _INT_CONST,
            OVER
        }
        int character = 0;
        // 两重循环，内层为状态机，外层负责不断启动状态机
        while (character != -1) {
            Status status = Status.START;
            StringBuilder builder = new StringBuilder();
            while (status != Status.OVER) {
                // 状态操作
                switch (status) {
                    case _INT -> tokens.add(Token.simple(TokenKind.fromString("int")));
                    case _RETURN -> tokens.add(Token.simple(TokenKind.fromString("return")));
                    case _OPERATOR_ASSIGN -> tokens.add(Token.simple(TokenKind.fromString("=")));
                    case _OPERATOR_COMMA -> tokens.add(Token.simple(TokenKind.fromString(",")));
                    case _SEMICOLON -> tokens.add(Token.simple(TokenKind.fromString("Semicolon")));
                    case _OPERATOR_ADD -> tokens.add(Token.simple(TokenKind.fromString("+")));
                    case _OPERATOR_SUB -> tokens.add(Token.simple(TokenKind.fromString("-")));
                    case _OPERATOR_MUL -> tokens.add(Token.simple(TokenKind.fromString("*")));
                    case _OPERATOR_DIV -> tokens.add(Token.simple(TokenKind.fromString("/")));
                    case _PARENTHESIS_LEFT -> tokens.add(Token.simple(TokenKind.fromString("(")));
                    case _PARENTHESIS_RIGHT -> tokens.add(Token.simple(TokenKind.fromString(")")));
                    case _ID -> {
                        String symbol = builder.toString();
                        tokens.add(Token.normal(TokenKind.fromString("id"), symbol));
                        try {
                            symbolTable.add(symbol);
                        } catch (RuntimeException e) {
                            System.out.println("Symbol " + symbol + " has been added to symbolTable.");
                        }
                        deque.addFirst(character);
                    }
                    case _INT_CONST -> {
                        tokens.add(Token.normal(TokenKind.fromString("IntConst"), builder.toString()));
                        deque.addFirst(character);
                    }
                    default -> {
                        // 读取下一个字符
                        character = deque.getFirst();
                        deque.removeFirst();
                        if (character != -1) {
                            if (status == Status.START && !Character.isWhitespace((char)character)      ||
                                    status == Status.SPACE && !Character.isWhitespace((char)character)  ||
                                    status == Status.DIGIT && Character.isDigit((char)character)    )   {
                                builder.append((char)character);
                            }
                            switch (status) {
                                case R, RE, RET, RETU, RETUR, RETURN, I, IN, INT, LETTER -> {
                                    if (Character.isLetter((char)character)) {
                                        builder.append((char)character);
                                    }
                                }
                            }
                        }
                    }
                }
                // 状态转移
                switch (status) {
                    case START, SPACE -> {
                        switch (character) {
                            case 'i' -> status = Status.I;
                            case 'r' -> status = Status.R;
                            case '=' -> status = Status._OPERATOR_ASSIGN;
                            case ',' -> status = Status._OPERATOR_COMMA;
                            case ';' -> status = Status._SEMICOLON;
                            case '+' -> status = Status._OPERATOR_ADD;
                            case '-' -> status = Status._OPERATOR_SUB;
                            case '*' -> status = Status._OPERATOR_MUL;
                            case '/' -> status = Status._OPERATOR_DIV;
                            case '(' -> status = Status._PARENTHESIS_LEFT;
                            case ')' -> status = Status._PARENTHESIS_RIGHT;
                            case -1  -> status = Status.OVER;
                            default -> {
                                if (Character.isWhitespace((char)character)) {
                                    status = Status.SPACE;
                                } if (Character.isDigit((char)character)) {
                                    status = Status.DIGIT;
                                } else if (Character.isLetter((char)character)) {
                                    status = Status.LETTER;
                                }
                            }
                        }
                    }
                    case I -> {
                        if (character == 'n') {
                            status = Status.IN;
                        } else if (Character.isLetter((char)character)) {
                            status = Status.LETTER;
                        } else {
                            status = Status._ID;
                        }
                    }
                    case IN -> {
                        if (character == 't') {
                            status = Status.INT;
                        } else if (Character.isLetter((char)character)) {
                            status = Status.LETTER;
                        } else {
                            status = Status._ID;
                        }
                    }
                    case INT -> {
                        if (Character.isLetter((char)character)) {
                            status = Status.LETTER;
                        } else {
                            status = Status._INT;
                        }
                    }
                    case R -> {
                        if (character == 'e') {
                            status = Status.RE;
                        } else if (Character.isLetter((char)character)) {
                            status = Status.LETTER;
                        } else {
                            status = Status._ID;
                        }
                    }
                    case RE -> {
                        if (character == 't') {
                            status = Status.RET;
                        } else if (Character.isLetter((char)character)) {
                            status = Status.LETTER;
                        } else {
                            status = Status._ID;
                        }
                    }
                    case RET -> {
                        if (character == 'u') {
                            status = Status.RETU;
                        } else if (Character.isLetter((char)character)) {
                            status = Status.LETTER;
                        } else {
                            status = Status._ID;
                        }
                    }
                    case RETU -> {
                        if (character == 'r') {
                            status = Status.RETUR;
                        } else if (Character.isLetter((char)character)) {
                            status = Status.LETTER;
                        } else {
                            status = Status._ID;
                        }
                    }
                    case RETUR -> {
                        if (character == 'n') {
                            status = Status.RETURN;
                        } else if (Character.isLetter((char)character)) {
                            status = Status.LETTER;
                        } else {
                            status = Status._ID;
                        }
                    }
                    case RETURN -> {
                        if (Character.isLetter((char)character)) {
                            status = Status.LETTER;
                        } else {
                            status = Status._RETURN;
                        }
                    }
                    case LETTER -> {
                        if (!Character.isLetter((char)character)) {
                            status = Status._ID;
                        }
                    }
                    case DIGIT -> {
                        if (!Character.isDigit((char)character)) {
                            status = Status._INT_CONST;
                        }
                    }
                    default -> status = Status.OVER;
                }
            }
        }
        tokens.add(Token.eof());
    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        return tokens;
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}
