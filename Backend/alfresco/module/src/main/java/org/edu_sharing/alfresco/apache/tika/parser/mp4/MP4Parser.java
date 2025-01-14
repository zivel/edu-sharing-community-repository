package org.edu_sharing.alfresco.apache.tika.parser.mp4;

import org.alfresco.util.TempFileProvider;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TemporaryResources;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.XMP;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.Box;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.Container;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.IsoFile;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleAlbumBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleArtist2Box;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleArtistBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleCommentBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleCompilationBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleDiskNumberBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleEncoderBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleGPSCoordinatesBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleGenreBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleItemListBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleNameBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleRecordingYear2Box;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleTrackAuthorBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.AppleTrackNumberBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.apple.Utf8AppleDataBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.iso14496.part12.FileTypeBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.iso14496.part12.MetaBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.iso14496.part12.MovieBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.iso14496.part12.SampleTableBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.iso14496.part12.TrackBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.iso14496.part12.TrackHeaderBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.iso14496.part12.UserDataBox;
import org.edu_sharing.repackaged.isoparser.org.mp4parser.boxes.sampleentry.AudioSampleEntry;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Parser for the MP4 media container format, as well as the older
 *  QuickTime format that MP4 is based on.
 *
 * This uses the MP4Parser project from http://code.google.com/p/mp4parser/
 *  to do the underlying parsing
 */
public class MP4Parser extends AbstractParser {
    /** Serial version UID */
    private static final long serialVersionUID = 84011216792285L;
    /** TODO Replace this with a 2dp Duration Property Converter */
    private static final DecimalFormat DURATION_FORMAT =
            (DecimalFormat)NumberFormat.getNumberInstance(Locale.ROOT);
    static {
        DURATION_FORMAT.applyPattern("0.0#");
    }
    // Ensure this stays in Sync with the entries in tika-mimetypes.xml
    private static final Map<MediaType,List<String>> typesMap = new HashMap<>();
    static {
        // All types should be 4 bytes long, space padded as needed
        typesMap.put(MediaType.audio("mp4"), Arrays.asList(
                "M4A ", "M4B ", "F4A ", "F4B "));
        typesMap.put(MediaType.video("3gpp"), Arrays.asList(
                "3ge6", "3ge7", "3gg6", "3gp1", "3gp2", "3gp3", "3gp4", "3gp5", "3gp6", "3gs7"));
        typesMap.put(MediaType.video("3gpp2"), Arrays.asList(
                "3g2a", "3g2b", "3g2c"));
        typesMap.put(MediaType.video("mp4"), Arrays.asList(
                "mp41", "mp42"));
        typesMap.put(MediaType.video("x-m4v"), Arrays.asList(
                "M4V ", "M4VH", "M4VP"));

        typesMap.put(MediaType.video("quicktime"), Collections.<String>emptyList());
        typesMap.put(MediaType.application("mp4"), Collections.<String>emptyList());
    }

    private static final Set<MediaType> SUPPORTED_TYPES =
            Collections.unmodifiableSet(typesMap.keySet());

    //edu-sharing quick fix
       /*
    private ISO6709Extractor iso6709Extractor = new ISO6709Extractor();
*/
    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return SUPPORTED_TYPES;
    }

    public void parse(
            InputStream stream, ContentHandler handler,
            Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {

        System.out.println("MP4Parser Using edu-sharing version");

        // The MP4Parser library accepts either a File, or a byte array
        // As MP4 video files are typically large, always use a file to
        //  avoid OOMs that may occur with in-memory buffering
        //TemporaryResources tmp = new TemporaryResources();
        //TikaInputStream tstream = TikaInputStream.get(stream, tmp);

        File file = null;
        try {
            file = TempFileProvider.createTempFile(stream, "edu", "tika");
        }catch(Exception e){
            e.printStackTrace();
        }

        //file = tstream.getFile();

        try (IsoFile isoFile = new IsoFile(file)) {
            //tmp.addResource(isoFile);

            // Grab the file type box
            FileTypeBox fileType = getOrNull(isoFile, FileTypeBox.class);
            if (fileType != null) {
                // Identify the type
                MediaType type = MediaType.application("mp4");
                for (Map.Entry<MediaType, List<String>> e : typesMap.entrySet()) {
                    if (e.getValue().contains(fileType.getMajorBrand())) {
                        type = e.getKey();
                        break;
                    }
                }
                metadata.set(Metadata.CONTENT_TYPE, type.toString());

                if (type.getType().equals("audio")) {
                    metadata.set(XMPDM.AUDIO_COMPRESSOR, fileType.getMajorBrand().trim());
                }
            } else {
                // Some older QuickTime files lack the FileType
                metadata.set(Metadata.CONTENT_TYPE, "video/quicktime");
            }


            // Get the main MOOV box
            MovieBox moov = getOrNull(isoFile, MovieBox.class);
            if (moov == null) {
                // Bail out
                return;
            }


            XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
            xhtml.startDocument();

            handleMovieHeaderBox(moov, metadata, xhtml);
            handleTrackBoxes(moov, metadata, xhtml);

            // Get metadata from the User Data Box
            UserDataBox userData = getOrNull(moov, UserDataBox.class);
            if (userData != null) {
                extractGPS(userData, metadata);
                MetaBox metaBox = getOrNull(userData, MetaBox.class);

                // Check for iTunes Metadata
                // See http://atomicparsley.sourceforge.net/mpeg-4files.html and
                //  http://code.google.com/p/mp4v2/wiki/iTunesMetadata for more on these
                handleApple(metaBox, metadata, xhtml);
                // TODO Check for other kinds too
            }

            // All done
            xhtml.endDocument();

        }catch(Throwable e){
            e.printStackTrace();
        } finally{
            //tmp.dispose();
        }

    }

    private void handleTrackBoxes(MovieBox moov, Metadata metadata, XHTMLContentHandler xhtml) {

        // Get some more information from the track header
        // TODO Decide how to handle multiple tracks
        List<TrackBox> tb = moov.getBoxes(TrackBox.class);
        if (tb == null || tb.size() == 0) {
            return;
        }
        TrackBox track = tb.get(0);

        TrackHeaderBox header = track.getTrackHeaderBox();
        // Get the creation and modification dates
        metadata.set(TikaCoreProperties.CREATED, header.getCreationTime());
        metadata.set(TikaCoreProperties.MODIFIED, header.getModificationTime());

        // Get the video with and height
        metadata.set(Metadata.IMAGE_WIDTH, (int) header.getWidth());
        metadata.set(Metadata.IMAGE_LENGTH, (int) header.getHeight());

        // Get the sample information
        SampleTableBox samples = track.getSampleTableBox();
        if (samples != null) {
            SampleDescriptionBox sampleDesc = samples.getSampleDescriptionBox();
            if (sampleDesc != null) {
                // Look for the first Audio Sample, if present
                AudioSampleEntry sample = getOrNull(sampleDesc, AudioSampleEntry.class);
                if (sample != null) {
                    XMPDM.ChannelTypePropertyConverter.convertAndSet(metadata, sample.getChannelCount());
                    //metadata.set(XMPDM.AUDIO_SAMPLE_TYPE, sample.getSampleSize());    // TODO Num -> Type mapping
                    metadata.set(XMPDM.AUDIO_SAMPLE_RATE, (int) sample.getSampleRate());
                    //metadata.set(XMPDM.AUDIO_, sample.getSamplesPerPacket());
                    //metadata.set(XMPDM.AUDIO_, sample.getBytesPerSample());
                }
            }
        }
    }

    private void handleMovieHeaderBox(MovieBox moov, Metadata metadata, XHTMLContentHandler xhtml) {
        // Pull out some information from the header box
        MovieHeaderBox mHeader = getOrNull(moov, MovieHeaderBox.class);
        if (mHeader == null) {
            return;
        }
        // Get the creation and modification dates
        metadata.set(TikaCoreProperties.CREATED, mHeader.getCreationTime());
        metadata.set(TikaCoreProperties.MODIFIED, mHeader.getModificationTime());

        // Get the duration
        double durationSeconds = ((double) mHeader.getDuration()) / mHeader.getTimescale();
        metadata.set(XMPDM.DURATION, DURATION_FORMAT.format(durationSeconds));

        // The timescale is normally the sampling rate
        metadata.set(XMPDM.AUDIO_SAMPLE_RATE, (int) mHeader.getTimescale());
    }

    private void handleApple(MetaBox metaBox, Metadata metadata, XHTMLContentHandler xhtml) throws SAXException {
        AppleItemListBox apple = getOrNull(metaBox, AppleItemListBox.class);
        if (apple == null) {
            return;
        }
        // Title
        AppleNameBox title = getOrNull(apple, AppleNameBox.class);
        addMetadata(TikaCoreProperties.TITLE, metadata, title);

        // Artist
        AppleArtistBox artist = getOrNull(apple, AppleArtistBox.class);
        addMetadata(TikaCoreProperties.CREATOR, metadata, artist);
        addMetadata(XMPDM.ARTIST, metadata, artist);

        // Album Artist
        //edu-sharing quick fix
       /* AppleArtist2Box artist2 = getOrNull(apple, AppleArtist2Box.class);
        addMetadata(XMPDM.ALBUM_ARTIST, metadata, artist2);*/

        // Album
        AppleAlbumBox album = getOrNull(apple, AppleAlbumBox.class);
        addMetadata(XMPDM.ALBUM, metadata, album);

        // Composer
        AppleTrackAuthorBox composer = getOrNull(apple, AppleTrackAuthorBox.class);
        addMetadata(XMPDM.COMPOSER, metadata, composer);

        // Genre
        AppleGenreBox genre = getOrNull(apple, AppleGenreBox.class);
        addMetadata(XMPDM.GENRE, metadata, genre);

        // Year
        AppleRecordingYear2Box year = getOrNull(apple, AppleRecordingYear2Box.class);
        if (year != null) {
            metadata.set(XMPDM.RELEASE_DATE, year.getValue());
        }

        // Track number
        AppleTrackNumberBox trackNum = getOrNull(apple, AppleTrackNumberBox.class);
        if (trackNum != null) {
            metadata.set(XMPDM.TRACK_NUMBER, trackNum.getA());
            //metadata.set(XMPDM.NUMBER_OF_TRACKS, trackNum.getB()); // TODO
        }

        // Disc number
        //edu-sharing quick fix
       /*
        AppleDiskNumberBox discNum = getOrNull(apple, AppleDiskNumberBox.class);
        if (discNum != null) {
            metadata.set(XMPDM.DISC_NUMBER, discNum.getA());
        }*/

        // Compilation
        //edu-sharing quick fix
       /*
        AppleCompilationBox compilation = getOrNull(apple, AppleCompilationBox.class);
        if (compilation != null) {
            metadata.set(XMPDM.COMPILATION, (int) compilation.getValue());
        }*/

        // Comment
        AppleCommentBox comment = getOrNull(apple, AppleCommentBox.class);
        addMetadata(XMPDM.LOG_COMMENT, metadata, comment);

        // Encoder
        AppleEncoderBox encoder = getOrNull(apple, AppleEncoderBox.class);
        if (encoder != null) {
            metadata.set(XMP.CREATOR_TOOL, encoder.getValue());
        }


        // As text
        for (Box box : apple.getBoxes()) {
            if (box instanceof Utf8AppleDataBox) {
                xhtml.element("p", ((Utf8AppleDataBox) box).getValue());
            }
        }

    }

    /**
     * Override the maximum record size limit.  NOTE: this
     * sets a static variable on the IsoFile and affects all files
     * parsed in this JVM!!!
     *
     * //@param maxRecordSize
     */
    //edu-sharing quick fix:
    // @Field
    /*public void setMaxRecordSize(long maxRecordSize) {
        IsoFile.MAX_RECORD_SIZE_OVERRIDE = maxRecordSize;
    }*/

    private void extractGPS(UserDataBox userData, Metadata metadata) {
        AppleGPSCoordinatesBox coordBox = getOrNull(userData, AppleGPSCoordinatesBox.class);
        if (coordBox == null) {
            return;
        }
        String iso6709 = coordBox.getValue();
      //edu-sharing quick fix:
        //  iso6709Extractor.extract(iso6709, metadata);
    }

    private static void addMetadata(Property prop, Metadata m, Utf8AppleDataBox metadata) {
        if (metadata != null) {
            m.set(prop, metadata.getValue());
        }
    }

    private static <T extends Box> T getOrNull(Container box, Class<T> clazz) {
        if (box == null) return null;

        List<T> boxes = box.getBoxes(clazz);
        if (boxes.size() == 0) {
            return null;
        }
        return boxes.get(0);
    }
}
