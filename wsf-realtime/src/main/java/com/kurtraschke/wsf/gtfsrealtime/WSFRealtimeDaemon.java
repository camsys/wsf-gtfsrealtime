package com.kurtraschke.wsf.gtfsrealtime;

import java.io.File;

import javax.inject.Inject;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeExporter;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeGuiceBindingTypes.Alerts;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeGuiceBindingTypes.TripUpdates;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeGuiceBindingTypes.VehiclePositions;
import org.onebusaway.guice.jsr250.LifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.ConfigurationException;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

/**
 * This class is intended to be called from jsvc or equivalent.
 *
 */
public class WSFRealtimeDaemon {
  private static final Logger _log = LoggerFactory.getLogger(WSFRealtimeDaemon.class);
  
  private static final String ARG_CONFIG_FILE = "config";

  @Inject
  @SuppressWarnings("unused")
  private WSFRealtimeProvider _provider;

  @Inject
  private LifecycleService _lifecycleService;

  private Injector _injector;

  @Inject
  @VehiclePositions
  private GtfsRealtimeExporter _vehiclePositionsExporter;

  @Inject
  @TripUpdates
  private GtfsRealtimeExporter _tripUpdatesExporter;

  @Inject
  @Alerts
  private GtfsRealtimeExporter _alertsExporter;

  private WSFRealtimeMain _main = null;
  private File _configFile = null;
  
  public void init(String[] arguments) {
    _main = new WSFRealtimeMain();

    ArgumentParser parser = ArgumentParsers.newArgumentParser("wsf-gtfsrealtime");
    parser.description("Produces a GTFS-realtime feed from the Washington State Ferries API");
    parser.addArgument("--" + ARG_CONFIG_FILE).type(File.class).help("configuration file path");
    Namespace parsedArgs;

    try {
      parsedArgs = parser.parseArgs(arguments);
      _configFile = parsedArgs.get(ARG_CONFIG_FILE);
    } catch (CreationException | ConfigurationException | ProvisionException e) {
      _log.error("Error in startup:", e);
      System.exit(-1);
    } catch (ArgumentParserException ex) {
      parser.handleError(ex);
    }    
  }
  
  public void start() {
    _main.run(_configFile);
  }
  
  public void stop() {
    _main.interrupt();
  }
  
  public void destroy() {
    _configFile = null;
    _main = null;
  }
}
