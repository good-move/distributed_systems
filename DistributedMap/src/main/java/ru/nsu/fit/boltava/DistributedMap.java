package ru.nsu.fit.boltava;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

public class DistributedMap extends ReceiverAdapter {

    private static final Logger LOG = LogManager.getLogger(DistributedMap.class);
    private static final int TIMEOUT = 10000;
    private static final String LOCK_NAME = "cluster-lock";

    private final Map<String, Double> map = new HashMap<>();
    private final RpcDispatcher dispatcher;
    private final Lock lock;

    public DistributedMap(JChannel channel) throws Exception {
        lock = new LockService(channel).getLock(LOCK_NAME);
        dispatcher = new RpcDispatcher(channel, this);
        dispatcher.setMembershipListener(this);
        dispatcher.setStateListener(this);
        dispatcher.start();
        channel.getState(null, TIMEOUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void viewAccepted(View view) {
        LOG.info("Change in membership: {}", view.getMembers());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getState(OutputStream output) throws Exception {
        var out = new DataOutputStream(output);
        lock.lock();
        try {
            synchronized (map) {
                LOG.info("Returning map of size {}", map.size());
                Util.objectToStream(map, out);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setState(InputStream input) throws Exception {
        var in = new DataInputStream(input);
        Map<String, Double> receivedMap = Util.objectFromStream(in);
        synchronized (map) {
            LOG.info("Receiving map of size {}", receivedMap.size());
            map.clear();
            map.putAll(receivedMap);
        }
    }

    public boolean putRecord(String key, double value) {
        synchronized (map) {
            map.put(key, value);
            LOG.info("map: set key={} to value={}", key, value);
        }
        return true;
    }

    public boolean popRecord(String key) {
        Double value;
        synchronized (map) {
            value = map.remove(key);
            LOG.info("map: remove entry key={}, value={}", key, value);
        }
        return value != null;
    }

    public boolean remove(String key) {
        try {
            lock.lock();
            RspList<Boolean> responses = dispatcher.callRemoteMethods(
                    null,
                    "popRecord",
                    new Object[]{key},
                    new Class[]{String.class},
                    RequestOptions.SYNC()
            );
            LOG.info("Responses: {}", responses);
            return responses.getSuspectedMembers().isEmpty()
                    && responses.getResults().stream().allMatch(Boolean::booleanValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public boolean put(String key, double value) {
        try {
            lock.lock();
            RspList<Boolean> responses = dispatcher.callRemoteMethods(
                    null,
                    "putRecord",
                    new Object[]{key, value},
                    new Class[]{String.class, double.class},
                    RequestOptions.SYNC()
            );
            LOG.info("Responses: {}", responses);
            return responses.getSuspectedMembers().isEmpty()
                    && responses.getResults().stream().allMatch(Boolean::booleanValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public Optional<Double> get(String key) {
        Double value;
        lock.lock();
        try {
            synchronized (map) {
                value = map.get(key);
            }
        } finally {
            lock.unlock();
        }
        return Optional.ofNullable(value);
    }

    public Set<Map.Entry<String, Double>> entrySet() {
        Set<Map.Entry<String, Double>> entrySet;
        synchronized (map) {
            entrySet = map.entrySet();
        }
        return entrySet;
    }

    public boolean compareAndSwap(String key, double oldValue, double newValue) {
        Double currentValue;
        lock.lock();
        try {
            currentValue = map.get(key);
            if (Objects.equals(currentValue, oldValue)) {
                RspList<Boolean> responses = dispatcher.callRemoteMethods(
                        null,
                        "putRecord",
                        new Object[]{key, newValue},
                        new Class[]{String.class, double.class},
                        RequestOptions.SYNC()
                );
                LOG.info("Responses: {}", responses);
                return responses.getSuspectedMembers().isEmpty()
                        && responses.getResults().stream().allMatch(Boolean::booleanValue);
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

}
