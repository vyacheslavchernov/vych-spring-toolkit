package ru.vych.http.impl.common;

/**
 * Константы стандартных MIME Media Types (IANA Media Types).
 * <p>
 * Класс не имеет публичных конструкторов и не может быть инстанциирован.
 * Содержит только {@code public static final String} константы для основных
 * MIME-типов, сгруппированных по категориям:
 * </p>
 * <ul>
 *   <li>Application — данные приложений (JSON, XML, PDF, архивы и т. д.)</li>
 *   <li>Text — текстовые данные</li>
 *   <li>Image — изображения</li>
 *   <li>Audio — аудио</li>
 *   <li>Video — видео</li>
 *   <li>Multipart — составные MIME-типы</li>
 *   <li>Font — шрифты</li>
 * </ul>
 *
 * @see ru.vych.http.impl.entities.Request.Builder
 * @see ru.vych.http.impl.entities.Header
 */
public final class MediaType {
    /** Wildcard MIME type. */
    public static final String WILDCARD = "*/*";

    /** Application JSON. */
    public static final String APPLICATION_JSON = "application/json";
    /** Application XML. */
    public static final String APPLICATION_XML = "application/xml";
    /** Application XHTML XML. */
    public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";
    /** Application SVG XML. */
    public static final String APPLICATION_SVG_XML = "application/svg+xml";
    /** Application Atom XML. */
    public static final String APPLICATION_ATOM_XML = "application/atom+xml";
    /** Application SOAP XML. */
    public static final String APPLICATION_SOAP_XML = "application/soap+xml";
    /** Application form URL-encoded. */
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    /** Application octet stream. */
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    /** Application PDF. */
    public static final String APPLICATION_PDF = "application/pdf";
    /** Application ZIP. */
    public static final String APPLICATION_ZIP = "application/zip";
    /** Application GZIP. */
    public static final String APPLICATION_GZIP = "application/gzip";
    /** Application Java archive. */
    public static final String APPLICATION_JAVA_ARCHIVE = "application/java-archive";
    /** Application JavaScript. */
    public static final String APPLICATION_JAVASCRIPT = "application/javascript";

    /** Text plain. */
    public static final String TEXT_PLAIN = "text/plain";
    /** Text HTML. */
    public static final String TEXT_HTML = "text/html";
    /** Text XML. */
    public static final String TEXT_XML = "text/xml";
    /** Text CSS. */
    public static final String TEXT_CSS = "text/css";
    /** Text CSV. */
    public static final String TEXT_CSV = "text/csv";
    /** Text JavaScript. */
    public static final String TEXT_JAVASCRIPT = "text/javascript";
    /** Text Markdown. */
    public static final String TEXT_MARKDOWN = "text/markdown";

    /** Image PNG. */
    public static final String IMAGE_PNG = "image/png";
    /** Image JPEG. */
    public static final String IMAGE_JPEG = "image/jpeg";
    /** Image GIF. */
    public static final String IMAGE_GIF = "image/gif";
    /** Image BMP. */
    public static final String IMAGE_BMP = "image/bmp";
    /** Image WebP. */
    public static final String IMAGE_WEBP = "image/webp";
    /** Image SVG XML. */
    public static final String IMAGE_SVG_XML = "image/svg+xml";
    /** Image X icon. */
    public static final String IMAGE_X_ICON = "image/x-icon";
    /** Image TIFF. */
    public static final String IMAGE_TIFF = "image/tiff";

    /** Audio MPEG. */
    public static final String AUDIO_MPEG = "audio/mpeg";
    /** Audio OGG. */
    public static final String AUDIO_OGG = "audio/ogg";
    /** Audio WAV. */
    public static final String AUDIO_WAV = "audio/wav";
    /** Audio WebM. */
    public static final String AUDIO_WEBM = "audio/webm";

    /** Video MP4. */
    public static final String VIDEO_MP4 = "video/mp4";
    /** Video MPEG. */
    public static final String VIDEO_MPEG = "video/mpeg";
    /** Video OGG. */
    public static final String VIDEO_OGG = "video/ogg";
    /** Video WebM. */
    public static final String VIDEO_WEBM = "video/webm";
    /** Video QuickTime. */
    public static final String VIDEO_QUICKTIME = "video/quicktime";

    /** Multipart form data. */
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    /** Multipart mixed. */
    public static final String MULTIPART_MIXED = "multipart/mixed";
    /** Multipart related. */
    public static final String MULTIPART_RELATED = "multipart/related";
    /** Multipart alternative. */
    public static final String MULTIPART_ALTERNATIVE = "multipart/alternative";

    /** Font TTF. */
    public static final String FONT_TTF = "font/ttf";
    /** Font OTF. */
    public static final String FONT_OTF = "font/otf";
    /** Font WOFF. */
    public static final String FONT_WOFF = "font/woff";
    /** Font WOFF2. */
    public static final String FONT_WOFF2 = "font/woff2";
}
