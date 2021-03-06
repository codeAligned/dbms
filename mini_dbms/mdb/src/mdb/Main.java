package mdb;

import Jakarta.util.FixDosOutputStream;
import Jakarta.util.Util;
import minidb.je.SystemHelpers;

import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.Vector;

//**************************************************
// Executing the main of Main will perform the following:
//1) Initialization.
//2) Parse input args and remove switches and their args.
//3) Call the driver() method.
//4) Call the cleanUp() method.
//**************************************************
//
    
public class Main {

    final static  Main instance = new  Main();
    static private int layerID_Counter = 0;
    static Vector switches = new Vector();
    static Vector posArgs = new Vector();

    final public static String packageName =  Main.getPackageName() ;

    private static URI baseURI ;

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    // Attribute "modelDirectory" is the base working directory as a File.
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //

    private static File modelDirectory = null ;

    //**************************************************
    // main
    //**************************************************
    static String packName = "";

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    // Attribute "baseLayer" is derived from the base directory name.
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //

    /**
     * Returns a valid Java identifier that represents the package or
     * layer name for the current file.  It is derived from the base
     * directory name, where non-java characters in the "path" are
     * replaced with dots (".")
     *
     * @layer<kernel>
     */
    public static String deriveLayerName() {

        final char DOT = '.' ;

        // Step 1: determine the relative path to the base directory:

        String base = Util.getFullPath( Main.getModelDirectory() ) ;

        File p = new File( kernelConstants.globals().currentAbsPath );
        String path = Util.getFullPath( p.getParentFile() ) ;
        if ( path.startsWith( base ) )
            path = path.substring( base.length() ) ;

        // Step 2: layer name by dropping illegal leading characters,
        // then replacing illegal character sequences with ".":

        StringBuffer layerName = new StringBuffer() ;

        int index = -1 ;
        while ( ++index < path.length() )
            if ( Character.isJavaIdentifierStart( path.charAt( index ) ) ) {
                layerName.append( path.charAt( index ) ) ;
                break ;
            }

        boolean haveDot = false ;
        while ( ++index < path.length() )
            if ( Character.isJavaIdentifierPart( path.charAt( index ) ) ) {
                layerName.append( path.charAt( index ) ) ;
                haveDot = false ;
            }
            else
                if ( ! haveDot ) {
                    layerName.append( DOT ) ;
                    haveDot = true ;
                }

        if ( haveDot )
            return layerName.substring( 0, layerName.length()-1 ) ;

        if ( layerName.length() < 1 ) {
            AstNode.error( "can't derive layer name" ) ;
            return "--unknown--" ;
        }

        return layerName.toString() ;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    // Methods to manipulate filenames as URIs:
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //

    public static String file2uri( String fileName ) {

        URI fileURI = new File( fileName ) . toURI() . normalize() ;

        String base = baseURI.getPath() ;
        String path = fileURI.getPath() ;
        int minSize = Math.min( base.length(), path.length() ) ;
            
        // Find first position after a slash at which base and path differ:
        //
        int diff = 0 ;
        while ( diff < minSize && base.charAt( diff ) == path.charAt( diff ) )
            ++ diff ;
        diff = 1 + base.lastIndexOf( '/', diff ) ;

        // Start a relative URI by first prefixing as many ".." segments
        // as needed to move from base to the common parent prefix:
        //
        StringBuffer uri = new StringBuffer() ;
        for ( int n = diff ; ( n = 1 + base.indexOf( '/', n ) ) > 0 ; )
            uri.append( "../" ) ;

        // Append the remaining (relative) path that leads to the file:
        //
        uri.append( path.substring( diff ) ) ;

        return uri.toString() ;
    }

    /**
     * Returns the base directory as a {@link File} object.
     *
     * @layer<kernel>
     */
    public static File getModelDirectory() {
        return modelDirectory ;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    // Attribute "packageName" is the package name of $TEqn.Main.
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //

    public static String getPackageName() {
        String pkg = instance.getClass().getName() ;
        int period = pkg.lastIndexOf( '.' ) ;
        return ( period > 0 )	? pkg.substring( 0, period ) : "" ;
    }

    public static void main( String args[] ) {
        int     argc    = args.length;
        int                non_switch_args;
        int                i, j;
        char               ch;
        AstProperties props;
        BaliParser         myParser = null;
        AstNode       root;
        PrintWriter        pw;
        String             line; // one line from the user
        String             input; // one Language command (terminated with line ".")
        ByteArrayInputStream is; // is and dis are used together
        DataInputStream      dis; // to "feed" the scanner.
        BufferedReader       userInput = null;
        BufferedReader       lastUserInput = null;

        // Step 1: print the Marquee...

        Class c = Main.class;
        String s = c.getName();
        int dot = s.indexOf( "." );
        String packageName = s.substring( 0, dot );
        System.out.println( packageName + " Started..." );

        // Step 2: a general routine to pick off command line options
        //         options are removed from command line and
        //         args array is adjusted accordingly.

        non_switch_args = 0;
        for ( i=0; i < argc; i++ ) {
            if ( args[i].charAt( 0 ) == '-' ) {

                // switches of form -xxxxx (where xxx is a sequence of 1
                // or more characters

                for ( j=1; j < args[i].length(); j++ ) {
                    if ( args[i].charAt( j ) == 'f' ) {
                        try {
                            userInput =
                           new BufferedReader( new FileReader( args[i+1] ) );
                            lastUserInput = userInput;
                        }
                        catch ( Exception e ) {
                            System.err.println( "File " + args[i+1] + " not found:" 
+ e.getMessage() );
                        }
                        i++;
                        break;
                    } else if ( args[i].charAt( j ) == 'c' ) {
                        //create database
                        File path = new File(args[i+1].trim().replaceAll("^\"|\"$", ""));
                        if(!path.exists())
                            path.mkdirs(); //make directory if it doesn't exist yet
                        i++;
                        break;
                    } else if ( args[i].charAt( j ) == 'd' ) {
                        File path = new File(args[i+1].trim().replaceAll("^\"|\"$", ""));
                        SystemHelpers.removeDirectory(path);
                        i++;
                        break;
                    } else
                        usage();
                }
            }
            else {
                // non-switch arg
                args[non_switch_args] = args[i];
                non_switch_args++;
            }
        }

        // Step 3: there must be at least one real argument, otherwise error

        if ( non_switch_args != 0 )
            usage();

        // Step 4: Initialize output stream
        //         Standard initialization stuff that should be
        //         platform independent.

        props = new AstProperties();
        String lineSeparator =
            System.getProperties().getProperty( "line.separator" );
        if ( lineSeparator.compareTo( "\n" ) != 0 )
            pw = new PrintWriter( new FixDosOutputStream( System.out ) );
        else
            pw = new PrintWriter( System.out );
        props.setProperty( "output", pw );
 
        // Step 5: Get input and parse until an empty line is entered.
        //         An empty line is something with "." only.
        boolean runningConsole = true; //to know that console needs to be switched back on.
        boolean runningScript = false; //to know that script is being run.

        if ( userInput == null ) {
            userInput = new BufferedReader( new InputStreamReader( System.in ) );
            runningConsole = true;
        }
        do {
            // LanguageName statement loop
            input = ""; // initialize input string

            // Step 6.1: print prompt

            System.out.print( "\n" + packageName + "> " );
            System.out.flush();

            // Step 5.2: collect in variable input over multiple line reads
            do {
                line = "";
                try {
                    line = userInput.readLine();
                    if(runningScript && line != null) System.out.println(line);
                }
                catch ( Exception e ) {
                    if (runningScript && runningConsole){
                        runningScript = false;
                        userInput = (lastUserInput == null) ? new BufferedReader( new InputStreamReader( System.in ) ): lastUserInput;
                        line = "";
                    }else { System.exit( 10 ); }
                }

                if(line == null && runningScript && runningConsole){
                    runningScript = false;
                    userInput = (lastUserInput == null) ? new BufferedReader( new InputStreamReader( System.in ) ): lastUserInput;
                    line = "";
                }

                if ( line == null )
                    break;
                if ( line.compareTo( "" ) == 0 )
                    continue;
                if ( line.compareTo( "." ) == 0 )
                    break;
                input += "\n" + line;
                System.out.print( " > " );
                System.out.flush();
            }
            while ( true );

            if ( input == "" )
                break;

            // Step 5.3: parse input string

            is  = new ByteArrayInputStream( input.getBytes() );
            dis = new DataInputStream( is );
            if ( myParser == null )
                myParser = new BaliParser( dis );
            else
                myParser.ReInit( dis );

            try {
                root = myParser.getStartRoot( myParser );
            }
            catch ( Throwable e ) {
                System.out.println( "Parsing Exception Thrown: " +
                  e.getMessage() );
                e.printStackTrace();
                continue; // go to next $(LanguageName) statement
            }
 
            // Step 5.4: Parse of input command succeeded!

            /*****************************************************
            * the following code should be removed and replaced  *
            * with some actionable code like:                    *
            *          ((SqlLang) root).execute();               *
            *****************************************************/

            if(( SqlLang ) root instanceof ScriptCmd){
                String scriptName = ((AstToken) (((ScriptCmd) ((ScriptCmd)( SqlLang ) root)).tok[1])).name.trim();
                runningScript = true;
                try {
                    userInput = new BufferedReader(new FileReader(scriptName.replace("\"","")));
                } catch (FileNotFoundException e) {
                    System.out.println("Filename " + scriptName + " not found at " +   new File(scriptName).getAbsolutePath());
                }
            }
            ( ( SqlLang ) root ).print();
            System.out.println();
//            System.out.println();
            long startTime = System.currentTimeMillis();
            ( ( SqlLang ) root ).execute();
            long endTime   = System.currentTimeMillis();
            System.out.println(" (time taken = "+ (endTime - startTime)/1000.0 +" sec)");
            pw.flush();

            // Step 5.5: now dump the parse tree
            //           this code can be removed for production systems

//            System.out.println( "Dump root" );
//            root.PrettyDump();

        }
        while ( true ); // end Language statement loop
    }

    public static void setBaseURI( String fileName ) {
        if ( fileName == null )
            fileName = "." ;
        baseURI = new File( fileName ) . toURI() . normalize() ;
    }

    /**
     * Sets the base directory to an absolute {@link File}.  If
     * <code>baseName</code> isn't an absolute path, it is resolved
     * relative to the current working directory.  If <code>baseName</code>
     * is <code>null</code>, the base directory is set to the current
     * working directory.
     *
     * @layer<kernel>
     */
    public static void setModelDirectory( String baseName ) {

        if ( baseName == null )
            baseName = "." ;

        modelDirectory = new File( baseName ) . getAbsoluteFile() ;
    }

    public static String uri2file( String uriName ) {
        File file = new File( baseURI.resolve( uriName ) ) ;
        return file.toString() ;
    }
    protected static void usage() {
        System.err.println( "Usage: java " + "$TEqn" +
               ".Main [-f file] [-create dbname] [-delete dbname]" );
        System.err.println( "       -f for input from file" );
        System.exit( -10 );
    }

    protected List extraArgs = null ;

    //**************************************************
    // Must be overridden. Each layer makes zero or more calls to
    // switchRegister() and posArgRegister().  All higher-level layers then
    // call Super(int).argInquire(nextLayer()); (See nextLayer() below.)
    //**************************************************
    //
    protected void argInquire( int _layer ) {

        switchRegister( new Switch( "base",
                    "specifies base working directory",
                    new String[] {"<base-working-directory>"},
                    true,
                    _layer ) ) ;

        switchRegister( new Switch( "a",
                    "specifies name of equation file -- .equation(s) are dropped if present",
                    new String[] {"<equation-file>"},
                    true,
                    _layer ) ) ;

        switchRegister( new Switch( "help",
                    "prints this helpful usage message",
                    null,
                    true,
                    _layer ) ) ;

    }

    protected void cleanUp() {
        // if we get to this point, there have been no fatal errors
        // but there may have been errors, and their numbers may have
        // accumulated if we have processed multiple files.  If
        // there are any errors at this time, then exit with an error
        // indicator (so that composer knows something went wrong).

        int nerrors =  AstNode.errorCount();
        if ( nerrors != 0 )
            System.exit( 1 );
    }

    //**************************************************
    // Methods called by driver().
    //**************************************************
    protected  AstNode createAST( ArgList argObjects ) {
        return ( null );
    }

    //**************************************************
    // Can override driver() and call Super().driver() in order to
    // do pre or post processing. The default driver simply calls
    // createAST(), then reduceAST(), then outputAST().
    // returns true if outputAST() is executed, false otherwise.
    // (meaning true if file was translated).
    //**************************************************
    protected boolean driver( ArgList arguments ) {
        AstNode ast;

        ast = createAST( arguments );
        if ( ast == null )
            return false;
        ast = reduceAST( arguments, ast );
        if ( ast == null )
            return false;
        outputAST( arguments, ast );
        return true;
    }

    //**************************************************
    // Initialize state prior any other processing.
    //**************************************************
    public void initialize() {}

    protected final int nextLayer() {
        return ( layerID_Counter++ );
    }
    protected void outputAST( ArgList argObjects,  AstNode ast ) {}

    //**************************************************
    // Parse input args. Remove switches and their args.
    //**************************************************
    protected ArgList parseArgs( String[] args ) {
        ArgList argObjects = new ArgList();
        int j,k;
        Switch sw;
        Switch newSwitch;
        String switchName;
        PositionalArg parg;

        for ( int i=0; i < args.length; i++ ) {
            if ( args[i].charAt( 0 ) == '-' ) {
                // switch
                switchName = args[i].substring( 1 );
                for ( j=0; j < switches.size(); j++ ) {
                    sw = ( Switch ) switches.elementAt( j );
                    if ( switchName.compareTo( sw.name ) == 0 ) {
                        // Found switch. Clone it.
                        try {
                            newSwitch = ( Switch ) sw.clone();
                        }
                        catch ( CloneNotSupportedException e ) {
                            Util.fatalError( e );
                            newSwitch = null;
                        }

                        // Bind args if any
                        if ( sw.args != null ) {
                            // Allocate array to hold args
                            newSwitch.args = new String[sw.args.length];

                            // Bind args from arg list
                            for ( k=0; k < sw.args.length; k++ ) {
                                if ( ++i == args.length )
                                    usage();
                                newSwitch.args[k] = args[i];
                            }
                        }

                        // Add newly created Switch object to argObjects.
                        argObjects.addElement( newSwitch );

                        break;
                    }
                } // end of for loop scanning switch list
            }
            else {
                // non-switch arg
                if ( posArgs.size() > 0 ) {
                    parg = ( PositionalArg ) posArgs.firstElement();
                    posArgs.removeElementAt( 0 );
                    parg.binding = args[i];

                    // Add existing PositionalArg object to argObjects.
                    argObjects.addElement( parg );
                }
                else
                    if ( extraArgs != null )
                        extraArgs.add( args [i] ) ;
                    else
                        usage() ;
            }
        }

        // Since we currently do not allow optional positional arguments,
        // make sure all required args have been supplied.
        if ( posArgs.size() != 0 )
            usage();

        // Print a usage message if requested:
        //
        if ( argObjects.find( "help",  Switch.class, 0 ) != null )
            usage() ;

        return ( argObjects );
    }
    protected final void posArgRegister( PositionalArg parg ) {
        posArgs.addElement( parg );
    }
    protected  AstNode reduceAST( ArgList argObjects,
                      AstNode ast ) {
        return ( ast );
    }

    // Services provided by top level. Cannot be overriden.
    protected final void switchRegister( Switch sw ) {
        switches.addElement( sw );
    }

}
