package plugins.tongli.Microscopy.MicroManager2.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.data.Coords;
import org.micromanager.data.internal.DefaultImage;

import icy.main.Icy;
import icy.sequence.Sequence;
import icy.util.DateUtil;
import mmcorej.TaggedImage;
import plugins.tongli.Microscopy.MicroManager2.MicroManager;
import plugins.tongli.Microscopy.MicroManager2.tools.MMUtils;

/**
 * This class is to be used by AcquisitionListener.</br>
 * It initializes an empty icy sequence you need to feed with the {@link #imageReceived(TaggedImage)} method so the
 * sequence will be updated with those images.
 * 
 * @author Stephane Dallongeville
 */
public class AcquisitionResult
{
    protected final Map<Integer, Sequence> sequences;
    protected final SequenceSettings settings;
    protected final JSONObject summaryMetadata;
    protected final long startTime;
    protected boolean done;

    public AcquisitionResult(SequenceSettings settings, JSONObject summaryMetadata)
    {
        this.settings = settings;
        this.summaryMetadata = summaryMetadata;
        sequences = new HashMap<Integer, Sequence>();
        startTime = Calendar.getInstance().getTimeInMillis();
        done = false;
    }

    public List<Sequence> getSequences()
    {
        return new ArrayList<Sequence>(sequences.values());
    }

    public void imageReceived(DefaultImage image) throws JSONException
    {
        final Coords coords = image.getCoords();
        final Integer position = Integer.valueOf(coords.getStagePosition());

        Sequence seq = sequences.get(position);
        if (seq == null)
        {
            // create a new sequence
            seq = new Sequence("Acquisition - " + DateUtil.now("yyyy-MM-dd HH'h'mm'm'ss's'"));
            sequences.put(position, seq);

            // display enabled ?
            if (MicroManager.getDisplayAcquisitionSequence())
                Icy.getMainInterface().addSequence(seq);
        }

        // set image
        MMUtils.setImage(seq, image.legacyToTaggedImage(), startTime);
        // first image ? --> try to get more informations from summary metadata
        if (seq.getNumImage() == 1)
            MMUtils.setMetadata(seq, summaryMetadata);
    }

    public boolean isDone()
    {
        return done;
    }

    public void done()
    {
        done = true;

        // complete some metadata
        for (Sequence seq : sequences.values())
        {
            // no time interval info ? -> set it from time position
            // if (seq.getTimeInterval() == 0d)
            // seq.setTimeInterval(MetaDataUtil.getTimeintervalFromTimePositions());
        }
    }
}