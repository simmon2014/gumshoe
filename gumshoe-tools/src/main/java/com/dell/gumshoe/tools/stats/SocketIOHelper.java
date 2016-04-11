package com.dell.gumshoe.tools.stats;

import com.dell.gumshoe.socket.io.SocketIODetailAdder;
import com.dell.gumshoe.stack.Stack;
import com.dell.gumshoe.stats.StatisticAdder;
import com.dell.gumshoe.tools.graph.IODirection;
import com.dell.gumshoe.tools.graph.IOUnit;
import com.dell.gumshoe.tools.graph.StackFrameNode;

import static com.dell.gumshoe.tools.Swing.*;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import java.awt.Color;
import java.awt.GridLayout;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

public class SocketIOHelper extends DataTypeHelper {
    private final JRadioButton readStat = new JRadioButton("read", true);
    private final JRadioButton writeStat = new JRadioButton("write");
    private final JRadioButton bothStat = new JRadioButton("read+write");
    private final JRadioButton opsUnit = new JRadioButton("ops/count", true);
    private final JRadioButton bytesUnit = new JRadioButton("bytes");
    private final JRadioButton timeUnit = new JRadioButton("time(ms)");

    @Override
    public String getToolTipText(StackFrameNode boxNode, StackFrameNode parentNode) {
        final SocketIODetailAdder boxDetail = (SocketIODetailAdder)boxNode.getDetail();
        final SocketIODetailAdder parentDetail = (SocketIODetailAdder)parentNode.getDetail();
        return String.format("<html>\n"
                                + "%s<br>\n"
                                + "%d addresses<br>\n"
                                + "R %d ops%s %d bytes%s %d ms%s<br>\n"
                                + "W %d ops%s %d bytes%s %d ms%s<br>\n"
                                + "R+W %d ops%s %d bytes%s %d ms%s<br>\n"
                                + "</html>",

                              boxNode.getFrame(),
                              boxDetail.addresses.size(),

                              boxDetail.readCount.get(), getPercent(boxDetail.readCount, parentDetail.readCount),
                              boxDetail.readBytes.get(), getPercent(boxDetail.readBytes, parentDetail.readBytes),
                              boxDetail.readTime.get(), getPercent(boxDetail.readTime, parentDetail.readTime),

                              boxDetail.writeCount.get(), getPercent(boxDetail.writeCount, parentDetail.writeCount),
                              boxDetail.writeBytes.get(), getPercent(boxDetail.writeBytes, parentDetail.writeBytes),
                              boxDetail.writeTime.get(), getPercent(boxDetail.writeTime, parentDetail.writeTime),

                              boxDetail.writeCount.get() + boxDetail.readCount.get(),
                              getPercent(boxDetail.writeCount, boxDetail.readCount, parentDetail.writeCount, parentDetail.readCount),
                              boxDetail.writeBytes.get() + boxDetail.readBytes.get(),
                              getPercent(boxDetail.writeBytes, boxDetail.readBytes, parentDetail.writeBytes, parentDetail.writeBytes),
                              boxDetail.writeTime.get() + boxDetail.readTime.get(),
                              getPercent(boxDetail.writeTime, boxDetail.readTime, parentDetail.writeTime, parentDetail.readTime));
    }

    @Override
    public String getDetailText(StackFrameNode boxNode, StackFrameNode parentNode) {
        final SocketIODetailAdder boxDetail = (SocketIODetailAdder)boxNode.getDetail();
        final SocketIODetailAdder parentDetail = (SocketIODetailAdder)parentNode.getDetail();
        final Set<StackTraceElement> callingFrames = boxNode.getCallingFrames();
        final Set<StackTraceElement> calledFrames = boxNode.getCalledFrames();
        return String.format("Frame: %s\n\n"
                                + "Network:\n%d addresses: %s\n\n"
                                + "Traffic:\nRead: %d operations%s, %d bytes%s, %d ms %s\n"
                                + "Write: %d operations%s, %d bytes%s, %d ms %s\n"
                                + "Combined: %d operations%s, %d bytes%s, %d ms %s\n\n"
                                + "Calls %d methods: %s\n\n"
                                + "Called by %d methods: %s",
                                boxNode.getFrame(),
                                boxDetail.addresses.size(), boxDetail.addresses.toString(),
                                boxDetail.readCount.get(), getPercent(boxDetail.readCount, parentDetail.readCount),
                                boxDetail.readBytes.get(), getPercent(boxDetail.readBytes, parentDetail.readBytes),
                                boxDetail.readTime.get(), getPercent(boxDetail.readTime, parentDetail.readTime),
                                boxDetail.writeCount.get(), getPercent(boxDetail.writeCount, parentDetail.writeCount),
                                boxDetail.writeBytes.get(), getPercent(boxDetail.writeBytes, parentDetail.writeBytes),
                                boxDetail.writeTime.get(), getPercent(boxDetail.writeTime, parentDetail.writeTime),
                                boxDetail.writeCount.get() + boxDetail.readCount.get(),
                                getPercent(boxDetail.writeCount, boxDetail.readCount, parentDetail.writeCount, parentDetail.readCount),
                                boxDetail.writeBytes.get() + boxDetail.readBytes.get(),
                                getPercent(boxDetail.writeBytes, boxDetail.readBytes, parentDetail.writeBytes, parentDetail.writeBytes),
                                boxDetail.writeTime.get() + boxDetail.readTime.get(),
                                getPercent(boxDetail.writeTime, boxDetail.readTime, parentDetail.writeTime, parentDetail.readTime),
                                calledFrames.size(), getFrames(calledFrames),
                                callingFrames.size(), getFrames(callingFrames) );
    }

    @Override
    public StatisticAdder parse(String value) throws ParseException {
        return SocketIODetailAdder.fromString(value);
    }

    @Override
    public String getSummary(Map<Stack, StatisticAdder> data) {
        SocketIODetailAdder tally = new SocketIODetailAdder();
        for(StatisticAdder item : data.values()) {
            tally.add((SocketIODetailAdder)item);
        }
        return data.size() + " entries, total " + tally;
    }

    @Override
    public JComponent getOptionEditor() {
        groupButtons(readStat, writeStat, bothStat);
//        final JPanel statPanel = stackWest(new JLabel("Operation:"), columns(readStat, writeStat, bothStat));
        final JPanel statPanel = columns(new JLabel("Operation: "), readStat, writeStat, bothStat);

        groupButtons(opsUnit, bytesUnit, timeUnit);
        final JPanel unitPanel = columns(new JLabel("Measurement: "), opsUnit, bytesUnit, timeUnit);

        return stackNorth(statPanel, unitPanel);
    }

    private IODirection getDirection() {
        if(readStat.isSelected()) return IODirection.READ;
        else if(writeStat.isSelected()) return IODirection.WRITE;
        else return IODirection.READ_PLUS_WRITE;
    }

    private IOUnit getUnit() {
        if(opsUnit.isSelected()) return IOUnit.OPS;
        else if(bytesUnit.isSelected()) return IOUnit.BYTES;
        else return IOUnit.TIME;
    }

    @Override
    public long getStatValue(StatisticAdder details) {
        return getValue((SocketIODetailAdder)details);
    }

    private long getValue(SocketIODetailAdder details) {
        switch(getDirection()) {
            case READ:              return getReadValue(details);
            case WRITE:             return getWriteValue(details);
            case READ_PLUS_WRITE:
            default:                return getReadValue(details) + getWriteValue(details);
        }
    }

    private long getReadValue(SocketIODetailAdder details) {
        switch(getUnit()) {
            case OPS:   return details.readCount.get();
            case BYTES: return details.readBytes.get();
            case TIME:
            default:    return details.readTime.get();
        }
    }

    private long getWriteValue(SocketIODetailAdder details) {
        switch(getUnit()) {
            case OPS:   return details.writeCount.get();
            case BYTES: return details.writeBytes.get();
            case TIME:
            default:    return details.writeTime.get();
        }
    }

}