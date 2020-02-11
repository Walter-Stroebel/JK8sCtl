/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 *
 * @author walter
 */
public class Maps {
    public static final ConcurrentSkipListMap<UUID,List<Metadata>> byUUID = new ConcurrentSkipListMap<>();
    public static final ConcurrentSkipListMap<String,List<Metadata>> byNAME = new ConcurrentSkipListMap<>();
    public static final ConcurrentSkipListMap<String,List<Metadata>> byNS = new ConcurrentSkipListMap<>();
}
