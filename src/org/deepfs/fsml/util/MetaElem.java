package org.deepfs.fsml.util;

import static org.basex.util.Token.string;
import org.deepfs.fsml.util.DeepFile.NS;
import org.basex.core.Main;
import org.basex.query.item.Type;

/**
 * Available metadata elements.
 * @author Bastian Lemke
 */
public enum MetaElem {

  /** Duration. */
  DURATION(NS.FSMETA, "duration", Type.DUR, false),

  // ----- date fields -------------------------------------------------------

  /** Other date. */
  DATE(NS.DCTERMS, "date", Type.DAT, false),

  /** Date of the last change made to a metadata attribute. */
  DATE_ATTRIBUTE_MODIFIED(NS.FSMETA, "dateAttributeModified", Type.DTM, false),
  /** Date of the last change made to the content. */
  DATE_CONTENT_MODIFIED(NS.FSMETA, "dateContentModified", Type.DTM, false),
  /** Date when the content was created. */
  DATE_CREATED(NS.FSMETA, "dateCreated", Type.DTM, false),
  /** Date of the last usage. */
  DATE_LAST_USED(NS.FSMETA, "dateLastUsed", Type.DTM, false),

  // ----- integer fields ----------------------------------------------------

  /** Group ID of the owner of the file. */
  FS_OWNER_GROUP_ID(NS.FSMETA, "fsOwnerGroupId", Type.ITR, false),
  /** User ID of the owner of the file. */
  FS_OWNER_USER_ID(NS.FSMETA, "fsOwnerUserId", Type.ITR, false),
  /** Size of the file in the file system. */
  FS_SIZE(NS.FSMETA, "fsSize", Type.ITR, false),
  /** Height in millimeters. */
  MM_HEIGHT(NS.FSMETA, "mmHeight", Type.ITR, false),
  /** Width in millimeters. */
  MM_WIDTH(NS.FSMETA, "mmWidth", Type.ITR, false),
  /** Number of pages. */
  NUMBER_OF_PAGES(NS.FSMETA, "numberOfPages", Type.ITR, false),
  /** Height in pixels. */
  PIXEL_HEIGHT(NS.FSMETA, "pixelHeight", Type.ITR, false),
  /** Width in pixels. */
  PIXEL_WIDTH(NS.FSMETA, "pixelWidth", Type.ITR, false),
  /** Track number. */
  TRACK(NS.FSMETA, "track", Type.ITR, false),

  // ----- string fields -----------------------------------------------------

  /** Abstract. */
  ABSTRACT(NS.DCTERMS, "abstract", Type.STR, true),
  /** Album name. */
  ALBUM(NS.FSMETA, "album", Type.STR, true),
  /** Alternative title. */
  ALTERNATIVE(NS.DCTERMS, "alternative", Type.STR, true),
  /** Comment. */
  COMMENT(NS.FSMETA, "comment", Type.STR, true),
  /** Composer. */
  COMPOSER(NS.FSMETA, "composer", Type.STR, true),
  /** Contributor. */
  CONTRIBUTOR(NS.DCTERMS, "contributor", Type.STR, true),
  /** Carbon copy receiver (name). */
  COPY_RECEIVER_NAME(NS.FSMETA, "copyReceiverName", Type.STR, true),
  /** Carbon copy receiver (email address). */
  COPY_RECEIVER_EMAIL(NS.FSMETA, "copyReceiverEmail", Type.STR, true),
  /** Creator (name). */
  CREATOR_NAME(NS.FSMETA, "creatorName", Type.STR, true),
  /** Creator (email address). */
  CREATOR_EMAIL(NS.FSMETA, "creatorEmail", Type.STR, true),
  /** Description. */
  DESCRIPTION(NS.DCTERMS, "description", Type.STR, true),
  /** Text encoding. */
  ENCODING(NS.FSMETA, "encoding", Type.STR, false),
  /** Genre. */
  GENRE(NS.FSMETA, "genre", Type.STR, true),
  /**
   * Headline. Publishable entry providing a synopsis of the contents of the
   * item.
   */
  HEADLINE(NS.FSMETA, "headline", Type.STR, false),
  /** Blind carbon copy receiver (name). */
  HIDDEN_RECEIVER_NAME(NS.FSMETA, "hiddenReceiverName", Type.STR, true),
  /** Blind carbon copy receiver (email address). */
  HIDDEN_RECEIVER_EMAIL(NS.FSMETA, "hiddenReceiverEmail", Type.STR, true),
  /** Unique identifier. */
  IDENTIFIER(NS.DCTERMS, "identifier", Type.STR, false),
  /** Keyword. */
  KEYWORD(NS.FSMETA, "keyword", Type.STR, true),
  /**
   * Language.
   * @see <a href="http://www.ietf.org/rfc/rfc4646.txt">RFC 4646</a>
   */
  LANGUAGE(NS.DCTERMS, "language", Type.STR, false),
  /** Lyricist. */
  LYRICIST(NS.FSMETA, "lyricist", Type.STR, true),

  // ----- location -----
  /** City. */
  CITY(NS.FSMETA, "city", Type.STR, true),
  /** Country. */
  COUNTRY(NS.FSMETA, "country", Type.STR, true),

  /** Publisher. */
  PUBLISHER(NS.DCTERMS, "publisher", Type.STR, true),
  /** Receiver (name). */
  RECEIVER_NAME(NS.FSMETA, "receiverName", Type.STR, true),
  /** Receiver (email address). */
  RECEIVER_EMAIL(NS.FSMETA, "receiverEmail", Type.STR, true),
  /** Sender (name). */
  SENDER_NAME(NS.FSMETA, "senderName", Type.STR, false),
  /** Sender (email address). */
  SENDER_EMAIL(NS.FSMETA, "senderEmail", Type.STR, false),
  /** Message or document subject. */
  SUBJECT(NS.DCTERMS, "subject", Type.STR, false),
  /** Table of contents. */
  TABLE_OF_CONTENTS(NS.DCTERMS, "tableOfContents", Type.STR, false),
  /** Title. */
  TITLE(NS.DCTERMS, "title", Type.STR, false),
  /** Type. */
  TYPE(NS.DCTERMS, "type", Type.STR, true),
  /** Format (MIME type). */
  FORMAT(NS.DCTERMS, "format", Type.STR, false),

  /** container element "content". */
  CONTENT(NS.DEEPURL, "content");

  /** Metadata key as byte array. */
  private final byte[] n;
  /** Namespace. */
  private final NS ns;
  /** Default XML data type. */
  private final Type dt;
  /** More precise data type. */
  private Type pdt;
  /** Flag, if the metadata element may have multiple values. */
  private final boolean multiVal;

  /*
   * content container element. private final TreeMap<MetaElem, byte[]> c;
   */

  /**
   * Constructor for key-value pairs.
   * @param name metadata key.
   * @param namespace namespace for the metadata attribute.
   * @param dataType xml datatype.
   * @param mv flag, if the metadata element may hava multiple values.
   */
  private MetaElem(final NS namespace, final String name, final Type dataType,
      final boolean mv) {
    ns = namespace;
    n = ns.tag(name);
    dt = dataType;
    multiVal = mv;
  }

  /**
   * Constructor for the content container element (map with several key-value
   * pairs).
   * @param namespace namespace for the container element.
   * @param name name of the container element.
   */
  private MetaElem(final NS namespace, final String name) {
    ns = namespace;
    n = ns.tag(name);
    dt = null;
    multiVal = false;
  }

  /**
   * Returns the metadata attribute name as byte array.
   * @return the metadata attribute name.
   */
  public byte[] get() {
    return n;
  }

  /**
   * Returns the xml datatype for the metadata attribute.
   * @return the xml datatype for the metadata attribute.
   */
  public Type getType() {
    if(pdt != null) return pdt;
    return dt;
  }

  /**
   * Returns true, if multiple values are allowed for the metadata attribute.
   * @return true, if multiple values are allowed for the metadata attribute.
   */
  public boolean isMultiVal() {
    return multiVal;
  }

  /**
   * Returns the content for a container element.
   * @return the content as map (containing key-value pairs). public
   *         TreeMap<MetaElem, byte[]> getContent() { return c; }
   */

  @Override
  public String toString() {
    return string(n);
  }

  /**
   * Override the default data type of the metadata element with a more precise
   * data type (e.g. "short" instead of "integer").
   * @param dataType the new xml data type to set for this metadata element.
   */
  void refineDataType(final Type dataType) {
    if(!dataType.instance(dt)) Main.bug("Failed to refine the xml data type "
        + "for the metadata element " + string(n) + " (invalid data type: "
        + dataType + ")");
    else pdt = dataType;
  }

  /**
   * Resets this metadata element to its default values (e.g. removes a
   * previously set refined xml data type).
   */
  void reset() {
    pdt = null;
  }
}
