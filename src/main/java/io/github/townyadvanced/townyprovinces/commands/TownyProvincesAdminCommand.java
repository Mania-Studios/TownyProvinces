package io.github.townyadvanced.townyprovinces.commands;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.jobs.dynmap_display.DynmapDisplayTaskController;
import io.github.townyadvanced.townyprovinces.jobs.land_validation.LandValidationJobStatus;
import io.github.townyadvanced.townyprovinces.jobs.land_validation.LandValidationTaskController;
import io.github.townyadvanced.townyprovinces.jobs.province_generation.RegenerateRegionTaskController;
import io.github.townyadvanced.townyprovinces.messaging.Messaging;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesPermissionNodes;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TownyProvincesAdminCommand implements TabExecutor {

	private static final List<String> adminTabCompletes = Arrays.asList("province","region", "landvalidationjob");
	private static final List<String> adminTabCompletesProvince = Arrays.asList("sea","land");
	private static final List<String> adminTabCompletesRegion = Arrays.asList("regenerate", "newtowncostperchunk", "upkeeptowncostperchunk");
	private static final List<String> adminTabCompletesSeaProvincesJob = Arrays.asList("status", "start", "stop", "restart", "pause");

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		switch (args[0].toLowerCase()) {
			case "province":
				if (args.length == 2)
					return NameUtil.filterByStart(adminTabCompletesProvince, args[1]);
				break;
			case "region":
				if (args.length == 2)
					return NameUtil.filterByStart(adminTabCompletesRegion, args[1]);
				if (args.length == 3) {
					//Create data folder if needed
					FileUtil.setupPluginDataFoldersIfRequired();
					//Create region definitions folder and sample files if needed
					FileUtil.createRegionDefinitionsFolderAndSampleFiles();
					//Reload region definitions
					TownyProvincesSettings.loadRegionDefinitions();
					List<String> regionOptions = new ArrayList<>();
					regionOptions.add("All");
					List<String> regionNames = new ArrayList<>(TownyProvincesSettings.getRegionDefinitions().keySet());
					Collections.sort(regionNames);
					regionOptions.addAll(regionNames);
					return NameUtil.filterByStart(regionOptions, args[2]);
				}
				break;
			case "landvalidationjob":
				if (args.length == 2)
					return NameUtil.filterByStart(adminTabCompletesSeaProvincesJob, args[1]);
				break;
			default:
				if (args.length == 1)
					return NameUtil.filterByStart(adminTabCompletes, args[0]);
		}
		return Collections.emptyList();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		parseAdminCommand(sender, args);
		return true;
	}

	private void parseAdminCommand(CommandSender sender, String[] args) {
		/*
		 * Parse Command.
		 */
		if (args.length > 0) {
			if (sender instanceof Player && !sender.hasPermission(TownyProvincesPermissionNodes.TOWNYPROVINCES_ADMIN.getNode(args[0]))) {
				Messaging.sendErrorMsg(sender, Translatable.of("msg_err_command_disable"));
				return;
			}
			switch (args[0]) {
				case "province":
					parseProvinceCommand(sender, StringMgmt.remFirstArg(args));
					break;
				case "region":
					parseRegionCommand(sender, StringMgmt.remFirstArg(args));
					break;
				case "landvalidationjob":
					parseLandValidationJobCommand(sender, StringMgmt.remFirstArg(args));
					break;

				/*
				 * Show help if no command found.
				 */
				default:
					showHelp(sender);
			}
		} else {
			if (sender instanceof Player && !sender.hasPermission(TownyProvincesPermissionNodes.TOWNYPROVINCES_ADMIN.getNode())) {
				Messaging.sendErrorMsg(sender, Translatable.of("msg_err_command_disable"));
				return;
			}
			showHelp(sender);
		}
	}

	private void showHelp(CommandSender sender) {
		TownyMessaging.sendMessage(sender, ChatTools.formatTitle("/townyprovincesadmin"));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpra", "province [sea|land] [<x>,<z>]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpra", "province [sea|land] [<x>,<z>] [<x>,<z>]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpra", "region [regenerate] [<Region Name>]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpra", "region [newtowncostperchunk] [<Region Name>] [amount]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpra", "region [upkeeptowncostperchunk] [<Region Name>] [amount]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpra", "landvalidationjob [status|start|stop|restart|pause]", ""));
	}

	private void parseProvinceCommand(CommandSender sender, String[] args) {
		if (args.length < 2) {
			showHelp(sender);
			return;
		}
		if (args[0].equalsIgnoreCase("sea")) {
			parseProvinceSetToSeaCommand(sender, args);
		} else if (args[0].equalsIgnoreCase("land")) {
			parseProvinceSetToLandCommand(sender,args);
		} else {
			showHelp(sender);
		}
	}
	
	private void parseRegionCommand(CommandSender sender, String[] args) {
		if (args.length < 2) {
			showHelp(sender);
			return;
		}
		if (args[0].equalsIgnoreCase("regenerate")) {
			parseRegionRegenerateCommand(sender, args);
		} else if (args[0].equalsIgnoreCase("newtowncostperchunk")) {
			Bukkit.getScheduler().runTaskAsynchronously(TownyProvinces.getPlugin(), () ->parseRegionSetNewTownCostCommand(sender, args));
		} else if (args[0].equalsIgnoreCase("upkeeptowncostperchunk")) {
			Bukkit.getScheduler().runTaskAsynchronously(TownyProvinces.getPlugin(), () -> parseRegionSetTownUpkeepCostCommand(sender, args));
		} else {
			showHelp(sender);
		}
	}

	private void parseLandValidationJobCommand(CommandSender sender, String[] args) {
		if (args.length < 1) {
			showHelp(sender);
			return;
		}
		if (args[0].equalsIgnoreCase("status")) {
			Translatable status = Translatable.of(LandValidationTaskController.getLandValidationJobStatus().getLanguageKey());
			Messaging.sendMsg(sender, Translatable.of("msg_land_validation_job_status").append(status));
			
		} else if (args[0].equalsIgnoreCase("start")) {
			if (LandValidationTaskController.getLandValidationJobStatus().equals(LandValidationJobStatus.STOPPED)
					|| LandValidationTaskController.getLandValidationJobStatus().equals(LandValidationJobStatus.PAUSED)) {
				Messaging.sendMsg(sender, Translatable.of("msg_land_validation_job_starting"));
				LandValidationTaskController.setLandValidationJobStatus(LandValidationJobStatus.START_REQUESTED);
				LandValidationTaskController.startTask();
			} else {
				Messaging.sendMsg(sender, Translatable.of("msg_err_command_not_possible_job_not_stopped_or_paused"));
			}
		
		} else if (args[0].equalsIgnoreCase("stop")) {
			if (LandValidationTaskController.getLandValidationJobStatus().equals(LandValidationJobStatus.STARTED)) {
				Messaging.sendMsg(sender, Translatable.of("msg_land_validation_job_stopping"));
				LandValidationTaskController.setLandValidationJobStatus(LandValidationJobStatus.STOP_REQUESTED);
			} else {
				Messaging.sendMsg(sender, Translatable.of("msg_err_command_not_possible_job_not_started"));
			}
			
		} else if (args[0].equalsIgnoreCase("pause")) {
			if (LandValidationTaskController.getLandValidationJobStatus().equals(LandValidationJobStatus.STARTED)) {
				Messaging.sendMsg(sender, Translatable.of("msg_land_validation_job_pausing"));
				LandValidationTaskController.setLandValidationJobStatus(LandValidationJobStatus.PAUSE_REQUESTED);
			} else {
				Messaging.sendMsg(sender, Translatable.of("msg_err_command_not_possible_job_not_started"));
			}

		} else if (args[0].equalsIgnoreCase("restart")) {
			if (LandValidationTaskController.getLandValidationJobStatus().equals(LandValidationJobStatus.STARTED)) {
				Messaging.sendMsg(sender, Translatable.of("msg_land_validation_job_restarting"));
				LandValidationTaskController.setLandValidationJobStatus(LandValidationJobStatus.RESTART_REQUESTED);
			} else {
				Messaging.sendMsg(sender, Translatable.of("msg_err_command_not_possible_job_not_started"));
			}
		} else {
			showHelp(sender);
		}
	}
	
	private void parseProvinceSetToSeaCommand(CommandSender sender, String[] args) {
		try {
			if (args.length == 2) {
				parseProvinceSetToSeaCommandByPoint(sender, args);
			} else if (args.length == 3){
				parseProvinceSetToSeaCommandByArea(sender, args);
			} else{
				showHelp(sender);
			}
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
		}
	}

	private void parseProvinceSetToLandCommand(CommandSender sender, String[] args) {
		try {
			if (args.length == 2) {
				parseProvinceSetToLandCommandByPoint(sender, args);
			} else if (args.length == 3){
				parseProvinceSetToLandCommandByArea(sender, args);
			} else{
				showHelp(sender);
			}
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
		}
	}
	
	private void parseProvinceSetToSeaCommandByPoint(CommandSender sender, String[] args) throws NumberFormatException, ArrayIndexOutOfBoundsException{
		String[] locationAsArray = args[1].split(",");
		if(locationAsArray.length != 2) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
			return;
		}
		int x = Integer.parseInt(locationAsArray[0]);
		int y = Integer.parseInt(locationAsArray[1]);
		Coord coord = Coord.parseCoord(x,y);
		Province province = TownyProvincesDataHolder.getInstance().getProvinceAt(coord.getX(), coord.getZ());
		//Validate action
		if(province == null) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
			return;
		}
		if(province.isSea()) {
			Messaging.sendMsg(sender, Translatable.of("msg_province_already_sea"));
			return;
		}
		//Set province to be sea
		province.setSea(true);
		province.saveData();
		DynmapDisplayTaskController.requestHomeBlocksRefresh();
		Messaging.sendMsg(sender, Translatable.of("msg_province_successfully_set_to_sea"));
	}

	private void parseProvinceSetToSeaCommandByArea(CommandSender sender, String[] args) {
		String[] topLeftCornerAsArray = args[1].split(",");
		if(topLeftCornerAsArray.length != 2) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
			return;
		}
		String[] bottomRightCornerAsArray = args[2].split(",");
		if(bottomRightCornerAsArray.length != 2) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
			return;
		}
		int topLeftX = Integer.parseInt(topLeftCornerAsArray[0]);
		int topLeftZ = Integer.parseInt(topLeftCornerAsArray[1]);
		int bottomRightX = Integer.parseInt(bottomRightCornerAsArray[0]);
		int bottomRightZ = Integer.parseInt(bottomRightCornerAsArray[1]);
		
		Set<Province> provinces = TownyProvincesDataHolder.getInstance().getProvincesInArea(topLeftX, topLeftZ, bottomRightX, bottomRightZ);
		
		for(Province province: provinces) {
			if(!province.isSea()) {
				province.setSea(true);
				province.saveData();
			}
		}
		DynmapDisplayTaskController.requestHomeBlocksRefresh();
		Messaging.sendMsg(sender, Translatable.of("msg_provinces_in_area_set_to_sea"));
	}
	
	private void parseRegionRegenerateCommand(CommandSender sender, String[] args) {
		//Create data folder if needed
		FileUtil.setupPluginDataFoldersIfRequired();
		//Create region definitions folder and sample files if needed
		FileUtil.createRegionDefinitionsFolderAndSampleFiles();
		//Reload region definitions
		TownyProvincesSettings.loadRegionDefinitions();
		//Verify the given region name
		String givenRegionName = args[1];
		String caseCorrectRegionName = TownyProvincesSettings.getCaseSensitiveRegionName(givenRegionName);
		if(givenRegionName.equalsIgnoreCase("all")) {
			RegenerateRegionTaskController.startTask(sender, givenRegionName);
		} else if(TownyProvincesSettings.getRegionDefinitions().containsKey(caseCorrectRegionName)) {
			RegenerateRegionTaskController.startTask(sender, caseCorrectRegionName);
		} else {
			Messaging.sendMsg(sender, Translatable.of("msg_err_unknown_region_name"));
		}
	}

	private void parseRegionSetNewTownCostCommand(CommandSender sender, String[] args) {
		try {
			String givenRegionName = args[1];
			double townCostPerChunk = Double.parseDouble(args[2]);
			String formattedTownCostPerChunk = TownyEconomyHandler.getFormattedBalance(townCostPerChunk);;
			double townCost;
			String caseCorrectRegionName = TownyProvincesSettings.getCaseSensitiveRegionName(givenRegionName);
			
			if(givenRegionName.equalsIgnoreCase("all")) {
				//Set cost for all provinces, regardless of region
				for (Province province : TownyProvincesDataHolder.getInstance().getProvincesSet()) {
					townCost = townCostPerChunk * province.getListOfCoordsInProvince().size();
					province.setNewTownCost(townCost);
					province.saveData();
				}
				DynmapDisplayTaskController.requestHomeBlocksRefresh();
				Messaging.sendMsg(sender, Translatable.of("msg_new_town_cost_set_for_all_regions", formattedTownCostPerChunk));

			} else if(TownyProvincesSettings.getRegionDefinitions().containsKey(caseCorrectRegionName)) {
				//Set cost for just one region
				for (Province province : TownyProvincesDataHolder.getInstance().getProvincesSet()) {
					if(TownyProvincesSettings.isProvinceInRegion(province, caseCorrectRegionName)) {
						townCost = townCostPerChunk * province.getListOfCoordsInProvince().size();
						province.setNewTownCost(townCost);
						province.saveData();
					}
				}
				DynmapDisplayTaskController.requestHomeBlocksRefresh();
				Messaging.sendMsg(sender, Translatable.of("msg_new_town_cost_set_for_one_region", caseCorrectRegionName, formattedTownCostPerChunk));
				
			} else {
				Messaging.sendMsg(sender, Translatable.of("msg_err_unknown_region_name"));
			}
		} catch (NumberFormatException nfe) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_value_must_be_and_integer"));
		}
	}
	
	private void parseRegionSetTownUpkeepCostCommand(CommandSender sender, String[] args) {
		try {
			String givenRegionName = args[1];
			double townCostPerChunk = Double.parseDouble(args[2]);
			String formattedTownCostPerChunk = TownyEconomyHandler.getFormattedBalance(townCostPerChunk);;
			double townCost;
			String caseCorrectRegionName = TownyProvincesSettings.getCaseSensitiveRegionName(givenRegionName);

			if(givenRegionName.equalsIgnoreCase("all")) {
				//Set cost for all provinces, regardless of region
				for (Province province : TownyProvincesDataHolder.getInstance().getProvincesSet()) {
					townCost = townCostPerChunk * province.getListOfCoordsInProvince().size();
					province.setUpkeepTownCost(townCost);
					province.saveData();
				}
				DynmapDisplayTaskController.requestHomeBlocksRefresh();
				Messaging.sendMsg(sender, Translatable.of("msg_upkeep_town_cost_set_for_all_regions", formattedTownCostPerChunk));

			} else if(TownyProvincesSettings.getRegionDefinitions().containsKey(caseCorrectRegionName)) {
				//Set cost for just one region
				for (Province province : TownyProvincesDataHolder.getInstance().getProvincesSet()) {
					if(TownyProvincesSettings.isProvinceInRegion(province, caseCorrectRegionName)) {
						townCost = townCostPerChunk * province.getListOfCoordsInProvince().size();
						province.setUpkeepTownCost(townCost);
						province.saveData();
					}
				}
				DynmapDisplayTaskController.requestHomeBlocksRefresh();
				Messaging.sendMsg(sender, Translatable.of("msg_upkeep_town_cost_set_for_one_region", caseCorrectRegionName, formattedTownCostPerChunk));

			} else {
				Messaging.sendMsg(sender, Translatable.of("msg_err_unknown_region_name"));
			}
		} catch (NumberFormatException nfe) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_value_must_be_and_integer"));
		}
	}
	
	private void parseProvinceSetToLandCommandByPoint(CommandSender sender, String[] args) {
		String[] locationAsArray = args[1].split(",");
		if(locationAsArray.length != 2) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
			return;
		}
		int x = Integer.parseInt(locationAsArray[0]);
		int y = Integer.parseInt(locationAsArray[1]);
		Coord coord = Coord.parseCoord(x,y);
		Province province = TownyProvincesDataHolder.getInstance().getProvinceAt(coord.getX(), coord.getZ());
		//Validate action
		if(province == null) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
			return;
		}
		if(!province.isSea()) {
			Messaging.sendMsg(sender, Translatable.of("msg_province_already_land"));
			return;
		}
		//Set province to be land
		province.setSea(false);
		province.saveData();
		DynmapDisplayTaskController.requestHomeBlocksRefresh();
		Messaging.sendMsg(sender, Translatable.of("msg_province_successfully_set_to_land"));
	}

	private void parseProvinceSetToLandCommandByArea(CommandSender sender, String[] args) {
		String[] topLeftCornerAsArray = args[1].split(",");
		if(topLeftCornerAsArray.length != 2) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
			return;
		}
		String[] bottomRightCornerAsArray = args[2].split(",");
		if(bottomRightCornerAsArray.length != 2) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
			return;
		}
		int topLeftX = Integer.parseInt(topLeftCornerAsArray[0]);
		int topLeftZ = Integer.parseInt(topLeftCornerAsArray[1]);
		int bottomRightX = Integer.parseInt(bottomRightCornerAsArray[0]);
		int bottomRightZ = Integer.parseInt(bottomRightCornerAsArray[1]);

		Set<Province> provinces = TownyProvincesDataHolder.getInstance().getProvincesInArea(topLeftX, topLeftZ, bottomRightX, bottomRightZ);

		for(Province province: provinces) {
			if(province.isSea()) {
				province.setSea(false);
				province.saveData();
			}
		}
		DynmapDisplayTaskController.requestHomeBlocksRefresh();
		Messaging.sendMsg(sender, Translatable.of("msg_provinces_in_area_set_to_land"));
	}
}

