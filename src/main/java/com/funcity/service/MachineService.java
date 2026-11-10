package com.funcity.service;

import com.funcity.dao.GameDAO;
import com.funcity.dao.MachineDAO;
import com.funcity.model.Game;
import com.funcity.model.Machine;
import com.funcity.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class MachineService {

    private static final Logger logger = LoggerFactory.getLogger(MachineService.class);
    private final MachineDAO machineDAO;
    private final GameDAO gameDAO;

    public MachineService() {
        this.machineDAO = new MachineDAO();
        this.gameDAO = new GameDAO();
    }

    public MachineService(MachineDAO machineDAO, GameDAO gameDAO) {
        this.machineDAO = machineDAO;
        this.gameDAO = gameDAO;
    }

    public Machine addMachine(String machineCode, int gameId, String serialNumber, String floorLocation, String status) {
        ValidationUtil.requireNonEmpty(machineCode, "Machine Code");
        ValidationUtil.requireNonEmpty(serialNumber, "Serial Number");
        ValidationUtil.requireNonEmpty(floorLocation, "Floor Location");

        Optional<Game> gameOpt = gameDAO.findById(gameId);
        if (gameOpt.isEmpty()) {
            throw new IllegalArgumentException("Game does not exist with ID: " + gameId);
        }

        if (machineDAO.findByCode(machineCode.trim()).isPresent()) {
            throw new IllegalArgumentException("Machine code already exists: " + machineCode);
        }

        Machine machine = new Machine(0, machineCode.trim(), gameId, serialNumber.trim(), floorLocation.trim(),
                status != null ? status : "OPERATIONAL");
        Machine created = machineDAO.create(machine);
        logger.info("Added new machine {} at {}", created.getMachineCode(), created.getFloorLocation());
        return created;
    }

    public boolean updateMachine(Machine machine) {
        ValidationUtil.requireNonEmpty(machine.getSerialNumber(), "Serial Number");
        ValidationUtil.requireNonEmpty(machine.getFloorLocation(), "Floor Location");
        return machineDAO.update(machine);
    }

    public boolean updateStatus(int machineId, String status) {
        logger.info("Updating machine {} status to {}", machineId, status);
        return machineDAO.updateStatus(machineId, status);
    }

    public boolean deleteMachine(int machineId) {
        return machineDAO.delete(machineId);
    }

    public Optional<Machine> getMachineById(int machineId) {
        return machineDAO.findById(machineId);
    }

    public Optional<Machine> getMachineByCode(String code) {
        return machineDAO.findByCode(code.trim());
    }

    public List<Machine> getAllMachines() {
        return machineDAO.findAll();
    }

    public List<Machine> getOperationalMachines() {
        return machineDAO.findOperational();
    }

    public List<Machine> getMachinesForGame(int gameId) {
        return machineDAO.findByGameId(gameId);
    }
}

