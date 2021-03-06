/*
 * WiFiAnalyzer
 * Copyright (C) 2018  VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.vrem.wifianalyzer.wifi.model;

import android.support.annotation.NonNull;

import com.vrem.util.EnumUtils;
import com.vrem.wifianalyzer.wifi.band.FrequencyPredicate;
import com.vrem.wifianalyzer.wifi.band.WiFiBand;
import com.vrem.wifianalyzer.wifi.band.WiFiChannel;
import com.vrem.wifianalyzer.wifi.band.WiFiWidth;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class WiFiSignal {
    public static final WiFiSignal EMPTY = new WiFiSignal(0, 0, WiFiWidth.MHZ_20, 0,"");
    public static final String FREQUENCY_UNITS = "MHz";

    private final int primaryFrequency;//主频率
    private final int centerFrequency;//中心频率
    private final WiFiWidth wiFiWidth;//wifi宽度 20MHz、40MHz
    private final WiFiBand wiFiBand;//wifi信道：2.4 GHZ/5 GHZ
    private final int level; //信号强度
    private final String channel;//信道

    public WiFiSignal(int primaryFrequency, int centerFrequency, @NonNull WiFiWidth wiFiWidth, int level,String channel) {
        this.primaryFrequency = primaryFrequency;
        this.centerFrequency = centerFrequency;
        this.wiFiWidth = wiFiWidth;
        this.level = level;
        this.wiFiBand = EnumUtils.find(WiFiBand.class, new FrequencyPredicate(primaryFrequency), WiFiBand.GHZ2);
        this.channel = channel;
    }

    public String getChannel(){
        return channel;
    }
    public int getPrimaryFrequency() {
        return primaryFrequency;
    }

    public int getCenterFrequency() {
        return centerFrequency;
    }

    public int getFrequencyStart() {
        return getCenterFrequency() - getWiFiWidth().getFrequencyWidthHalf();
    }

    public int getFrequencyEnd() {
        return getCenterFrequency() + getWiFiWidth().getFrequencyWidthHalf();
    }

    public WiFiBand getWiFiBand() {
        return wiFiBand;
    }

    public WiFiWidth getWiFiWidth() {
        return wiFiWidth;
    }

    public WiFiChannel getPrimaryWiFiChannel() {
        return getWiFiBand().getWiFiChannels().getWiFiChannelByFrequency(getPrimaryFrequency());
    }

    public WiFiChannel getCenterWiFiChannel() {
        return getWiFiBand().getWiFiChannels().getWiFiChannelByFrequency(getCenterFrequency());
    }

    public int getLevel() {
        return level;
    }

    public Strength getStrength() {
        return Strength.calculate(level);
    }

    public double getDistance() {
        return WiFiUtils.calculateDistance(getPrimaryFrequency(), getLevel());
    }

    public boolean isInRange(int frequency) {
        return frequency >= getFrequencyStart() && frequency <= getFrequencyEnd();
    }

    @NonNull
    public String getChannelDisplay() {
        int primaryChannel = getPrimaryWiFiChannel().getChannel();
        int centerChannel = getCenterWiFiChannel().getChannel();
        String wifiChannel = Integer.toString(primaryChannel);
        if (primaryChannel != centerChannel) {
            wifiChannel += "(" + Integer.toString(centerChannel) + ")";
        }
        return wifiChannel.equals("0") ? channel : wifiChannel;//第一次进来会返回当前连接wifi的信，之后返回前置传进来的信道
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        WiFiSignal that = (WiFiSignal) o;

        return new EqualsBuilder()
            .append(getPrimaryFrequency(), that.getPrimaryFrequency())
            .append(getWiFiWidth(), that.getWiFiWidth())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getPrimaryFrequency())
            .append(getWiFiWidth())
            .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
