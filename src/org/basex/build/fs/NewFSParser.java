//package org.basex.build.fs;
//
//import static org.basex.data.DataText.*;
//import static org.basex.util.Token.*;
//import java.io.File;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.TreeMap;
//import org.basex.build.Builder;
//import org.basex.build.Parser;
//import org.basex.core.Main;
//import org.basex.core.Prop;
//import org.basex.core.Text;
//import org.basex.core.proc.CreateFS;
//import org.basex.io.IO;
//import org.basex.io.IOFile;
//import org.basex.util.Atts;
//import org.basex.util.TokenBuilder;
//import org.deepfs.fs.DeepFS;
//import org.deepfs.fsml.extractors.SpotlightExtractor;
//import org.deepfs.fsml.parsers.IFileParser;
//import org.deepfs.fsml.parsers.TXTParser;
//import org.deepfs.fsml.util.BufferedFileChannel;
//import org.deepfs.fsml.util.DeepFile;
//import org.deepfs.fsml.util.Loader;
//import org.deepfs.fsml.util.MetaElem;
//import org.deepfs.fsml.util.ParserUtil;
//import org.deepfs.util.LibraryLoader;
//
///**
// * Imports/shreds/parses a file hierarchy into a database. In more detail
// * importing a file hierarchy means to map a file hierarchy into an XML
// * representation according to an XML document valid against the DeepFSML
// * specification. This class {@link NewFSParser} instantiates the parsers to
// * extract metadata and content from files.
// * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
// * @author Alexander Holupirek
// * @author Bastian Lemke
// */
//public final class NewFSParser extends Parser {
//
//  /** Registry for MetadataAdapter implementations. */
//  static final Map<String, Class<? extends IFileParser>> REGISTRY =
//      new HashMap<String, Class<? extends IFileParser>>();
//
//  /** Fallback parser for file suffixes that are not registered. */
//  static Class<? extends IFileParser> fallbackParser;
//
//  /**
//   * Registers a parser implementation with the fs parser.
//   * @param suffix the suffix to register the parser implementation for.
//   * @param c the parser implementation class.
//   */
//  public static void register(final String suffix,
//      final Class<? extends IFileParser> c) {
//    REGISTRY.put(suffix, c);
//  }
//
//  /**
//   * Registers a fallback parser implementation with the fs parser.
//   * @param c the parser implementation class.
//   */
//  public static void registerFallback(final Class<? extends IFileParser> c) {
//    if(fallbackParser != null) {
//      Main.debug("Replacing fallback parser with " + c.getName());
//    }
//    fallbackParser = c;
//  }
//
//  /** Spotlight extractor. */
//  private SpotlightExtractor spotlight;
//
////  static {
////    try {
////      final Class<?>[] classes = Loader.load(IFileParser.class.getPackage(),
////          IFileParser.class);
////      for(final Class<?> c : classes) {
////        final String name = c.getSimpleName();
////        if(REGISTRY.containsValue(c)) {
////          Main.debug("Successfully loaded parser: %", name);
////        } else if(fallbackParser == c) {
////          Main.debug("Successfully loaded fallback parser: %", name);
////        } else Main.debug("Loading % ... FAILED", name);
////      }
////    } catch(final IOException ex) {
////      Main.errln("Failed to load parsers (%)", ex.getMessage());
////    }
////  }
//
//  /** If true, verbose debug messages are created (e.g. for corrupt files). */
//  public static final boolean VERBOSE = true;
//
//  /** Empty attribute array. */
//  private static final Atts EMPTY_ATTS = new Atts();
//  /** Directory size Stack. */
//  private final long[] sizeStack = new long[IO.MAXHEIGHT];
//  /** Stack for the size attribute ids of content elements. */
//  private final int[] contentSizeIdStack = new int[IO.MAXHEIGHT];
//  /** Path to root of the backing store. */
//  private final String backingpath;
//  /** Instantiated parsers. */
//  private final Map<String, IFileParser> parserInstances;
//  /** Instantiated fallback parser. */
//  private IFileParser fallbackParserInstance;
//  /** The buffer to use for parsing the file contents. */
//  private final ByteBuffer buffer;
//
//  /** Root flag to parse root node or all partitions (C:, D: ...). */
//  private final boolean root;
//  /** Reference to the database builder. */
//  private Builder builder;
//  /** The currently processed file. */
//  public File curr;
//  /** Level counter. */
//  private int lvl;
//  /** Do not expect complete file hierarchy, but parse single files. */
//  private boolean singlemode;
//
//  /**
//   * First byte of the current file or content element in the current file. Is
//   * always equals to 0 for file elements.
//   */
//  private long lastContentOffset;
//  /**
//   * Size of the current file or content element in the current file. For file
//   * elements this value is equals to the file size.
//   */
//  private long lastContentSize;
//  /** Counts how many content elements have been opened. */
//  private int contentOpenedCounter;
//
//  /**
//   * Constructor.
//   * @param path of import root (backing store)
//   * @param pr database properties
//   */
//  public NewFSParser(final String path, final Prop pr) {
//    super(IO.get(path), pr);
//    prop.set(Prop.INTPARSE, true);
//    prop.set(Prop.ENTITY, false);
//    prop.set(Prop.DTD, false);
//    root = path.equals("/");
//
//    backingpath = root ? "" : io.path();
//
//    // SPOTLIGHT must not be true if the library is not available
//    if(prop.is(Prop.SPOTLIGHT)) {
//      if(!Prop.MAC) prop.set(Prop.SPOTLIGHT, false);
//      if(!LibraryLoader.isLoaded(LibraryLoader.SPOTEXLIBNAME)) {
//        try {
//          // initialize SpotlightExtractor class and try to load the library
//          Class.forName(SpotlightExtractor.class.getCanonicalName(), true,
//              ClassLoader.getSystemClassLoader());
//        } catch(final ClassNotFoundException e) { /* */}
//        if(!LibraryLoader.isLoaded(LibraryLoader.SPOTEXLIBNAME)) prop.set(
//            Prop.SPOTLIGHT, false);
//      }
//    }
//
//    if(prop.is(Prop.FSMETA) || prop.is(Prop.FSCONT)) {
//      buffer = ByteBuffer.allocateDirect(IO.BLOCKSIZE);
//      if(prop.is(Prop.SPOTLIGHT)) {
//        spotlight = new SpotlightExtractor();
//        fallbackParserInstance = new TXTParser();
//      } else {
//        final int size = (int) Math.ceil(REGISTRY.size() / 0.75f);
//        parserInstances = new HashMap<String, IFileParser>(size);
//        return;
//      }
//    } else {
//      buffer = null;
//    }
//    parserInstances = null;
//  }
//
//  /**
//   * Gets a parser implementation for given file suffix.
//   * @param suffix the file suffix to get the parser for.
//   * @return the parser implementation or <code>null</code> if no
//   *         implementation is available.
//   */
//  private IFileParser getParser(final String suffix) {
//    IFileParser instance = parserInstances.get(suffix);
//    if(instance == null) {
//      final Class<? extends IFileParser> clazz = REGISTRY.get(suffix);
//      if(clazz == null) return null;
//      try {
//        instance = clazz.newInstance();
//        Main.debug("Successfully initialized parser for ." + suffix
//            + " files: " + clazz.getSimpleName());
//      } catch(final InstantiationException ex) {
//        Main.debug("Failed to load parser for suffix " + suffix + " (% - %)",
//            clazz.getSimpleName(), ex.getMessage());
//      } catch(final IllegalAccessException ex) {
//        Main.debug("Failed to load parser for suffix " + suffix + " (% - %)",
//            clazz.getSimpleName(), ex.getMessage());
//      }
//      // put in hash map ... even if null
//      parserInstances.put(suffix, instance);
//    }
//    return instance;
//  }
//
//  /**
//   * Gets the fallback parser implementation.
//   * @return the fallback parser implementation or <code>null</code> if no
//   *         fallback parser is available.
//   */
//  private IFileParser getFallbackParser() {
//    if(fallbackParser == null) return null;
//    if(fallbackParserInstance == null) {
//      try {
//        fallbackParserInstance = fallbackParser.newInstance();
//        Main.debug("Successfully initialized fallback parser.");
//      } catch(final InstantiationException ex) {
//        Main.debug("Failed to load fallback parser (%)", ex.getMessage());
//      } catch(final IllegalAccessException ex) {
//        Main.debug("Failed to load fallback parser (%)", ex.getMessage());
//      }
//    }
//    return fallbackParserInstance;
//  }
//
//  /**
//   * Main entry point for the import of a file hierarchy. Instantiates the
//   * engine and starts the traversal.
//   * @param build instance passed by {@link CreateFS}.
//   * @throws IOException I/O exception
//   */
//  @Override
//  public void parse(final Builder build) throws IOException {
//    builder = build;
//    builder.encoding(Prop.ENCODING);
//    builder.meta.backing = backingpath;
//    builder.meta.deepfs = true;
//    builder.startDoc(token(io.name()));
//
//    if(singlemode) {
//      file(new File(io.path()).getCanonicalFile());
//    } else {
//      atts.reset();
//      atts.add(MOUNTPOINT, NOTMOUNTED);
//      atts.add(BACKINGSTORE, token(backingpath));
//      atts.add(SIZE, ZERO);
//
//      final int sizeAttId = builder.startElem(DEEPFS_NS, atts) + 3;
//
//      for(final File f : root ? File.listRoots() : new File[] { new File(
//          backingpath).getCanonicalFile()}) {
//
//        if(f.isHidden() && !f.getAbsolutePath().equals("C:\\")) continue;
//        sizeStack[0] = 0;
//
//        for(final File file : f.listFiles()) {
//          if(!valid(file) || file.isHidden()) continue;
//          if(file.isDirectory()) dir(file);
//          else file(file);
//        }
//
//      }
//      setSize(sizeAttId, sizeStack[0]);
//      builder.endElem(DEEPFS_NS);
//    }
//    builder.endDoc();
//  }
//
//  /**
//   * Adds the size value to the current node.
//   * @param id the ID of the size attribute.
//   * @param size the size to set.
//   * @throws IOException I/O exception.
//   */
//  private void setSize(final int id, final long size) throws IOException {
//    builder.setAttValue(id, token(size));
//  }
//
//  /**
//   * Determines if the specified file is valid and no symbolic link.
//   * @param f file to be tested.
//   * @return true for a symbolic link
//   */
//  private static boolean valid(final File f) {
//    try {
//      return f.getPath().equals(f.getCanonicalPath());
//    } catch(final IOException ex) {
//      Main.debug(f + ": " + ex.getMessage());
//      return false;
//    }
//  }
//
//  /**
//   * Invoked when a directory is visited.
//   * @param d directory name
//   * @throws IOException I/O exception
//   */
//  private void dir(final File d) throws IOException {
//    final File[] files = d.listFiles();
//    if(files == null) return;
//
//    final int sizeAttId = builder.startElem(DIR_NS, DeepFS.atts(d)) + 2;
//    sizeStack[++lvl] = 0;
//
//    for(final File f : files) {
//      if(!valid(f) || f.isHidden()) continue;
//      if(f.isDirectory()) dir(f);
//      else file(f);
//    }
//
//    final long size = sizeStack[lvl];
//    setSize(sizeAttId, size);
//    builder.endElem(DIR_NS);
//
//    // add file size to parent folder
//    sizeStack[--lvl] += size;
//  }
//
//  /**
//   * Invoked when a regular file is visited.
//   * @param f file name
//   * @throws IOException I/O exception
//   */
//  private void file(final File f) throws IOException {
//    curr = f;
//    final long size = f.length();
//    final boolean meta = prop.is(Prop.FSMETA);
//    final boolean content = prop.is(Prop.FSCONT);
//
//    if(!singlemode) {
//      final String name = f.getName();
//      builder.startElem(FILE_NS, DeepFS.atts(f));
//      if((prop.is(Prop.FSMETA) || prop.is(Prop.FSCONT)) && f.canRead()
//          && f.isFile()) {
//        if(prop.is(Prop.SPOTLIGHT)) {
//          if(prop.is(Prop.FSMETA)) spotlight.parse(f);
//          if(prop.is(Prop.FSCONT)) {
//            try {
//              final BufferedFileChannel fc =
//                new BufferedFileChannel(f, buffer);
//              try {
//                fallbackParserInstance.readContent(fc, this);
//              } finally {
//                try {
//                  fc.close();
//                } catch(final IOException e) { /* */}
//              }
//            } catch(final IOException ex) {
//              Main.debug("Failed to parse file: %", ex);
//            }
//          }
//        } else if(name.indexOf('.') != -1) { // internal parser
//          final int dot = name.lastIndexOf('.');
//          final String suffix = name.substring(dot + 1).toLowerCase();
//          if(size > 0) {
//            IFileParser parser = getParser(suffix);
//            if(parser == null) parser = getFallbackParser();
//            if(parser != null) {
//              try {
//                final BufferedFileChannel fc = new BufferedFileChannel(f,
//                    buffer);
//                try {
//                  lastContentOffset = 0;
//                  lastContentSize = size;
//                  contentOpenedCounter = 0;
//                  parse0(parser, fc);
//                } finally {
//                  try {
//                    fc.close();
//                  } catch(final IOException e1) { /* */}
//                }
//              } catch(final Exception ex) {
//                Main.debug("Failed to parse file (%): %", f.getAbsolutePath(),
//                    ex);
//              }
//            }
//          } // end if size > 0
//        } // end internal parser
//      } // end if FSMETA/FSCONT
//      builder.endElem(FILE_NS);
//    }
//    // add file size to parent folder
//    sizeStack[lvl] += size;
//  }
//
//  /**
//   * <p>
//   * Parses a fragment of a file, e.g. a picture inside an ID3 frame.
//   * </p>
//   * <p>
//   * This method is intended to be called only from within a parser
//   * implementation. The parser implementation must create a subchannel of its
//   * {@link BufferedFileChannel} instance via
//   * {@link BufferedFileChannel#subChannel(int)}.
//   * </p>
//   * @param df the DeepFile to fill with metadata and contents.
//   * @param suffix the file suffix(es). More than one suffix means that the
//   *          file type is unknown. All given suffixes will be tested.
//   * @throws IOException if any error occurs while reading from the file.
//   */
//  public void parseFileFragment(final DeepFile df, final String... suffix)
//      throws IOException {
//    if(df.fsmeta || df.fscont) {
//      IFileParser parser = null;
//      final BufferedFileChannel bfc = df.getBufferedFileChannel();
//      for(final String s : suffix) {
//        final IFileParser p = getParser(s);
//        try {
//          if(p.check(bfc)) parser = p;
//        } finally {
//          bfc.reset();
//        }
//      }
//      if(parser == null) return;
//      parser.extract(df);
//      bfc.finish();
//      // final long offset = f.absolutePosition();
//      // final long size = f.size();
//      // startContent(offset, size);
//      // if(title != null) builder.nodeAndText(MetaElem.TITLE.get(),
//      //   EMPTY_ATTS,
//      // token(title));
//      // if(parser != null) {
//      // try {
//      // lastContentOffset = offset;
//      // lastContentSize = size;
//      // parse0(parser, bfc);
//      // } catch(final Exception ex) {
//      // Main.debug(
//      // "Failed to parse file fragment (file: %, offset: %, length: %): "
//      // + "%", bfc.getFileName(), offset, size, ex);
//      // bfc.finish();
//      // }
//      // }
//      // endContent();
//    }
//  }
//
//  /**
//   * <p>
//   * Parses the file with the fallback parser.
//   * </p>
//   * <p>
//   * This method is intended to be called from a parser implementation that
//   * failed to parse a file.
//   * </p>
//   * @param df the {@link DeepFile} to fill with metadata and contents.
//   * @throws IOException if any error occurs while reading from the file.
//   */
//  public void parseWithFallbackParser(final DeepFile df) throws IOException {
//    final IFileParser parser = getFallbackParser();
//    if(parser == null) return;
//    final BufferedFileChannel bfc = df.getBufferedFileChannel();
//    bfc.reset();
//    parser.extract(df);
//    bfc.finish();
//  }
//
//  /**
//   * Starts the parser implementation.
//   * @param parser the parser instance.
//   * @param deepFile the {@link DeepFile} to fill with metadata and contents.
//   * @throws IOException if any error occurs while reading from the file.
//   */
//  private void parse0(final IFileParser parser, final DeepFile deepFile)
//      throws IOException {
//    bf.reset();
//    if(prop.is(Prop.FSMETA)) {
//      if(prop.is(Prop.FSCONT)) parser.readMetaAndContent(bf, this);
//      else parser.readMeta(bf, this);
//    } else if(prop.is(Prop.FSCONT)) parser.readContent(bf, this);
//    bf.finish();
//  }
//
//  @Override
//  public String tit() {
//    return Text.CREATEFSPROG;
//  }
//
//  @Override
//  public String det() {
//    return curr != null ? curr.toString() : "";
//  }
//
//  @Override
//  public double prog() {
//    return 0;
//  }
//
//  /**
//   * Deletes a non-empty directory.
//   * @param dir to be deleted.
//   * @return boolean true for success, false for failure.
//   */
//  public static boolean deleteDir(final File dir) {
//    if(dir.isDirectory()) {
//      for(final String child : dir.list()) {
//        if(!deleteDir(new File(dir, child))) return false;
//      }
//    }
//    return dir.delete();
//  }
//
//  // -------------------------------------------------------------------------
//  // -------------------------------------------------------------------------
//  // -------------------------------------------------------------------------
//
//  /**
//   * Generates the xml representation for a map with key-value pairs that
//   * contains the metadata information for the current file.
//   * @param metaElements {@link TreeMap}, containing metadata information for
//   *          the current file.
//   * @throws IOException if any error occurs.
//   */
//  public void addMeta(final TreeMap<MetaElem, byte[]> metaElements)
//      throws IOException {
//    if(!prop.is(Prop.FSMETA)) return;
//    for(final Map.Entry<MetaElem, byte[]> entry : metaElements.entrySet())
//      builder.nodeAndText(entry.getKey().get(), EMPTY_ATTS, entry.getValue());
//  }
//
//  /**
//   * Adds a text element.
//   * @param offset the absolute position of the first byte of the file
//   *          fragment represented by this content element inside the current
//   *          file. A negative value stands for an unknown offset.
//   * @param size the size of the content element.
//   * @param text the text to add.
//   * @param preserveSpace if true, the xml attribute <code>xml:space</code> is
//   *          set.
//   * @throws IOException if any error occurs while reading from the file.
//   */
//  public void textContent(final long offset, final long size,
//      final String text, final boolean preserveSpace) throws IOException {
//    textContent(offset, size, token(text), preserveSpace);
//  }
//
//  /**
//   * Adds a text element.
//   * @param offset the absolute position of the first byte of the file
//   *          fragment represented by this content element inside the current
//   *          file. A negative value stands for an unknown offset.
//   * @param size the size of the content element.
//   * @param text the text to add.
//   * @param preserveSpace if true, the xml attribute <code>xml:space</code> is
//   *          set.
//   * @throws IOException if any error occurs while reading from the file.
//   */
//  public void textContent(final long offset, final long size,
//      final byte[] text, final boolean preserveSpace) throws IOException {
//    textContent(offset, size, new TokenBuilder(ParserUtil.checkUTF(text)),
//        preserveSpace);
//  }
//
//  /**
//   * <p>
//   * Adds a text element. <b><code>text</code> must contain only valid UTF-8
//   * characters!</b> Otherwise the generated XML document may be not
//   * well-formed.
//   * </p>
//   * @param offset the absolute position of the first byte of the file
//   *          fragment represented by this content element inside the current
//   *          file. A negative value stands for an unknown offset.
//   * @param size the size of the content element.
//   * @param text the text to add.
//   * @param preserveSpace if true, the xml attribute <code>xml:space</code> is
//   *          set.
//   * @throws IOException if any error occurs while reading from the file.
//   */
//  @SuppressWarnings("all")
//  // suppress dead code warning for ADD_ATTS
//  public void textContent(final long offset, final long size,
//      final TokenBuilder text, final boolean preserveSpace)
//         throws IOException {
//    if(!prop.is(Prop.FSCONT)) return;
//    atts.reset();
//    atts.add(OFFSET, offset >= 0 ? token(offset) : UNKNOWN);
//    atts.add(SIZE, token(size));
//    if(!preserveSpace) text.chop();
//    if(text.size() == 0) return;
//    builder.startElem(TEXT_CONTENT_NS, atts);
//    builder.text(text, false);
//    builder.endElem(TEXT_CONTENT_NS);
//  }
//
//  /**
//   * <p>
//   * Generates the XML representation for a new content element inside the
//   * current file or content node node with a preliminary size value. The size
//   * value may be set with {@link #setContentSize(long) setContentSize(size)}.
//   * If it's not set, the size is supposed to be unknown.
//   * </p>
//   * @param offset the absolute position of the first byte of the file
//   *          fragment represented by this content element inside the current
//   *          file. A negative value stands for an unknown offset.
//   * @throws IOException if any I/O error occurs.
//   */
//  public void startContent(final long offset) throws IOException {
//    if(!prop.is(Prop.FSCONT)) return;
//    atts.reset();
//    atts.add(OFFSET, offset >= 0 ? token(offset) : UNKNOWN);
//    atts.add(SIZE, UNKNOWN);
//    contentSizeIdStack[contentOpenedCounter++] = builder.startElem(CONTENT_NS,
//        atts) + 2;
//    return;
//  }
//
//  /**
//   * Sets the size value for the last opened content element.
//   * @param size the size value to set.
//   * @throws IOException if any I/O error occurs.
//   */
//  public void setContentSize(final long size) throws IOException {
//    if(!prop.is(Prop.FSCONT)) return;
//    builder.setAttValue(contentSizeIdStack[contentOpenedCounter - 1],
//        token(size));
//  }
//
//  /**
//   * <p>
//   * Generates the xml representation for a new content element inside the
//   * current file or content node node.
//   * </p>
//   * @param offset the absolute position of the first byte of the file
//   *          fragment represented by this content element inside the current
//   *          file. A negative value stands for an unknown offset.
//   * @param size the size of the content element.
//   * @throws IOException if any I/O error occurs.
//   */
//  public void startContent(final long offset, final long size)
//      throws IOException {
//    if(!prop.is(Prop.FSCONT)) return;
//    if(size < 1)
//      throw new IllegalArgumentException("content size must be > 0");
//    if(offset == lastContentOffset && size == lastContentSize) {
//      /*
//       * content range is exactly the same as the range of the parent element.
//       * So don't create a new element and insert everything in the actual
//       * element.
//       */
//      return;
//    }
//    startContent(offset);
//    setContentSize(size);
//  }
//
//  /**
//   * Closes the last opened content element.
//   * @throws IOException if any error occurs while reading from the file.
//   */
//  public void endContent() throws IOException {
//    if(!prop.is(Prop.FSCONT)) return;
//    if(contentOpenedCounter > 0) {
//      builder.endElem(CONTENT_NS);
//      contentOpenedCounter--;
//    }
//  }
//
//  /**
//   * Generates the xml representation for a new XML content element inside the
//   * current file or content node node.
//   * @param offset the absolute position of the first byte of the
//   *   file fragment
//   *          represented by this content element inside the current file.
//   * @param size the size of the content element.
//   * @throws IOException if any error occurs while reading from the file.
//   */
//  public void startXMLContent(final long offset, final long size)
//      throws IOException {
//    if(!prop.is(Prop.FSCONT)) return;
//    atts.reset();
//    atts.add(OFFSET, offset >= 0 ? token(offset) : UNKNOWN);
//    atts.add(SIZE, token(size));
//    builder.startElem(XML_CONTENT_NS, atts);
//  }
//
//  /**
//   * Closes the last opened XML content element.
//   * @throws IOException if any error occurs while reading from the file.
//   */
//  public void endXMLContent() throws IOException {
//    if(!prop.is(Prop.FSCONT)) return;
//    builder.endElem(XML_CONTENT_NS);
//  }
//
//  /**
//   * Checks if a parser for the given suffix is available and the file
//   * is in the correct format.
//   * @param f the {@link BufferedFileChannel} to check.
//   * @param suffix the file suffix.
//   * @return true if the data is supported.
//   * @throws IOException if any error occurs while reading from the file.
//   */
//  public boolean isParseable(final BufferedFileChannel f, final String suffix)
//      throws IOException {
//    final IFileParser parser = getParser(suffix);
//    if(parser == null) return false;
//    final long pos = f.position();
//    final boolean res = parser.check(f);
//    f.position(pos);
//    return res;
//  }
//}
