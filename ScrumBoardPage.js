'use client';

import { useState } from 'react';
import './ScrumBoardPage.css';

export default function TableManagement() {
  const [activeTab, setActiveTab] = useState('tables');
  const [searchTerm, setSearchTerm] = useState('');
  const [showAddTableForm, setShowAddTableForm] = useState(false);
  const [tableName, setTableName] = useState('');
  const [columns, setColumns] = useState([]);
  const [currentColumn, setCurrentColumn] = useState({
    columnName: '',
    datatype: 'String',
    length: '',
    scale: '',
    notNull: false,
    primaryKey: false,
    defaultValue: '',
    isUserUpdatable: false,
    isDGEnabled: false
  });
  const [successMessage, setSuccessMessage] = useState('');
  const [selectedModule, setSelectedModule] = useState('Demand Planning');
  const [nValuedTableMappings, setNValuedTableMappings] = useState([
    { name: 'DP_INTELLICAST', sourceColumns: 104, targetColumns: 100 },
    { name: 'DP_HISTORIC_SALES', sourceColumns: 104, targetColumns: 110 },
    { name: 'DP_HISTORIC_FORECAST', sourceColumns: 104, targetColumns: 100 },
    { name: 'DP_Table1', sourceColumns: 104, targetColumns: 103 }
  ]);
  const [modules, setModules] = useState([
    {
      name: 'Demand Planning',
      tableCount: 10,
      isExpanded: true,
      tables: [
        'DP_INTELLICAST',
        'DP_HISTORIC_SALES',
        'DP_HISTORIC_FORECAST',
        'DP_Table1',
        'DP_Table2',
        'DP_Table1',
        'DP_Table1',
        'DP_Table1',
        'DP_Table2',
        'DP_Table1',
      ]
    },
    {
      name: 'Replenishment Planning',
      tableCount: 3,
      isExpanded: false,
      tables: ['RP_Table1', 'RP_Table2', 'RP_Table3']
    },
    {
      name: 'Data Integrator',
      tableCount: 3,
      isExpanded: false,
      tables: ['DI_Table1', 'DI_Table2', 'DI_Table3']
    },
    {
      name: 'Order Management',
      tableCount: 3,
      isExpanded: false,
      tables: ['OM_Table1', 'OM_Table2', 'OM_Table3']
    },
    {
      name: 'Workforce Management',
      tableCount: 3,
      isExpanded: false,
      tables: ['WM_Table1', 'WM_Table2', 'WM_Table3']
    },
    {
      name: 'Tradeflow',
      tableCount: 3,
      isExpanded: false,
      tables: ['TF_Table1', 'TF_Table2', 'TF_Table3']
    },
    {
      name: 'Business Layer',
      tableCount: 3,
      isExpanded: false,
      tables: ['BL_Table1', 'BL_Table2', 'BL_Table3']
    }
  ]);

  const toggleModule = (index) => {
    const newModules = [...modules];
    newModules[index].isExpanded = !newModules[index].isExpanded;
    setModules(newModules);
  };

  const handleAddColumn = () => {
    if (currentColumn.columnName.trim() && tableName.trim()) {
      setColumns([...columns, currentColumn]);
      setCurrentColumn({
        columnName: '',
        datatype: 'String',
        length: '',
        scale: '',
        notNull: false,
        primaryKey: false,
        defaultValue: '',
        isUserUpdatable: false,
        isDGEnabled: false
      });
    }
  };

  const handleSubmit = () => {
    if (tableName.trim() && columns.length > 0) {
      setSuccessMessage('Table created successfully!');
      setTimeout(() => {
        resetForm();
      }, 2000);
    }
  };

  const resetForm = () => {
    setShowAddTableForm(false);
    setTableName('');
    setColumns([]);
    setCurrentColumn({
      columnName: '',
      datatype: 'String',
      length: '',
      scale: '',
      notNull: false,
      primaryKey: false,
      defaultValue: '',
      isUserUpdatable: false,
      isDGEnabled: false
    });
    setSuccessMessage('');
  };

  const handleCancel = () => {
    resetForm();
  };

  const filteredModules = modules.map(module => ({
    ...module,
    tables: module.tables.filter(table =>
      table.toLowerCase().includes(searchTerm.toLowerCase())
    )
  }));

  return (
    <div className="container-fluid table-management-container">
      {/* Tabs */}
      <div className="tabs-section mb-4">
        <button
          className={`tab-btn ${activeTab === 'columns' ? 'active' : ''}`}
          onClick={() => setActiveTab('columns')}
        >
          User Configured Columns
        </button>
        <button
          className={`tab-btn ${activeTab === 'tables' ? 'active' : ''}`}
          onClick={() => setActiveTab('tables')}
        >
          User Configured Tables
        </button>
        <button
          className={`tab-btn ${activeTab === 'nvalued' ? 'active' : ''}`}
          onClick={() => setActiveTab('nvalued')}
        >
          N-VALUED TABLES
        </button>
      </div>

      <div className="main-container">
        {/* Left Sidebar */}
        <div className="left-sidebar">
          <div className="modules-list">
            {filteredModules.map((module, index) => (
              <div key={index} className="module-section">
                {/* Module Header */}
                <div 
                  className={`module-header ${module.isExpanded ? 'expanded' : ''}`}
                  onClick={() => toggleModule(index)}
                >
                  <div className="header-left">
                    <span className="expand-icon">
                      {module.isExpanded ? '▼' : '▶'}
                    </span>
                    <div className="color-bar"></div>
                    <span className="module-name">{module.name}</span>
                  </div>
                  <span className="table-count">{module.tableCount} Tables</span>
                </div>

                {/* Search Box - Only in first expanded module */}
                {module.isExpanded && index === 0 && (
                  <div className="search-box-wrapper">
                    <input
                      type="text"
                      placeholder="Enter a keyword to search"
                      className="search-input"
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    <span className="search-icon">🔍</span>
                  </div>
                )}

                {/* Tables List */}
                {module.isExpanded && (
                  <div className="tables-list">
                    {module.tables.length > 0 ? (
                      module.tables.map((table, tableIndex) => (
                        <div key={tableIndex} className="table-item">
                          <span className="table-name">{table}</span>
                          {tableIndex === 0 && (
                            <span className="table-badge">Real Access Only</span>
                          )}
                        </div>
                      ))
                    ) : (
                      <div className="no-results">No tables found</div>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Right Panel */}
        <div className="right-panel">
          {activeTab === 'nvalued' ? (
            <div className="nvalued-tables-view">
              <div className="nvalued-header">
                <h3 className="nvalued-title">{selectedModule}</h3>
                <button className="btn btn-primary btn-rebuild-table">
                  Re-Build Table
                </button>
              </div>

              <div className="nvalued-content">
                <div className="nvalued-left-panel">
                  <h4 className="nvalued-section-title">Source Tables</h4>
                  <div className="source-tables-list">
                    {modules.find(m => m.name === selectedModule)?.tables.slice(0, 10).map((table, idx) => (
                      <div key={idx} className="source-table-item">
                        <span className="source-table-name">{table}</span>
                        <span className="column-count">104 Columns</span>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="nvalued-right-panel">
                  <h4 className="nvalued-section-title">N-Valued Mapping</h4>
                  <div className="mapping-list">
                    {nValuedTableMappings.map((mapping, idx) => (
                      <div key={idx} className="mapping-row">
                        <span className="mapping-source-name">{mapping.name}</span>
                        <span className="mapping-source-count">{mapping.sourceColumns}</span>
                        <span className="mapping-arrow">→</span>
                        <span className="mapping-target-count">{mapping.targetColumns}</span>
                        <span className="mapping-label">Columns</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          ) : !showAddTableForm ? (
            <div className="panel-header">
              <button 
                className="btn btn-primary btn-add-table"
                onClick={() => setShowAddTableForm(true)}
              >
                + Add Table
              </button>
            </div>
          ) : (
            <div className="add-table-form-container">
              {/* Success Message */}
              {successMessage && (
                <div className="success-message">
                  {successMessage}
                </div>
              )}

              <div className="form-and-columns">
                {/* Left Side - Form */}
                <div className="form-section">
                  <div className="form-group">
                    <label>Table Name</label>
                    <span className="form-separator">:</span>
                    <input 
                      type="text" 
                      placeholder=""
                      value={tableName}
                      onChange={(e) => setTableName(e.target.value)}
                      className="form-control"
                    />
                  </div>

                  <div className="form-group">
                    <label>Column Name</label>
                    <span className="form-separator">:</span>
                    <input 
                      type="text" 
                      placeholder=""
                      value={currentColumn.columnName}
                      onChange={(e) => setCurrentColumn({...currentColumn, columnName: e.target.value})}
                      className="form-control"
                    />
                  </div>

                  <div className="form-group">
                    <label>Datatype</label>
                    <span className="form-separator">:</span>
                    <select 
                      value={currentColumn.datatype}
                      onChange={(e) => setCurrentColumn({...currentColumn, datatype: e.target.value})}
                      className="form-control"
                    >
                      <option>Select the Datatype</option>
                      <option>String</option>
                      <option>Integer</option>
                      <option>Float</option>
                      <option>Date</option>
                      <option>Boolean</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label>Length</label>
                    <span className="form-separator">:</span>
                    <input 
                      type="text" 
                      placeholder="Enter the length"
                      value={currentColumn.length}
                      onChange={(e) => setCurrentColumn({...currentColumn, length: e.target.value})}
                      className="form-control"
                    />
                  </div>

                  <div className="form-group">
                    <label>Scale</label>
                    <span className="form-separator">:</span>
                    <input 
                      type="text" 
                      placeholder="Enter the length"
                      value={currentColumn.scale}
                      onChange={(e) => setCurrentColumn({...currentColumn, scale: e.target.value})}
                      className="form-control"
                    />
                  </div>

                  <div className="form-group">
                    <label>Not Null</label>
                    <span className="form-separator">:</span>
                    <div className="checkbox-group">
                      <label className="checkbox-label">
                        <input 
                          type="radio" 
                          name="notNull"
                          checked={currentColumn.notNull === true}
                          onChange={() => setCurrentColumn({...currentColumn, notNull: true})}
                        />
                        Yes
                      </label>
                      <label className="checkbox-label">
                        <input 
                          type="radio" 
                          name="notNull"
                          checked={currentColumn.notNull === false}
                          onChange={() => setCurrentColumn({...currentColumn, notNull: false})}
                        />
                        No
                      </label>
                    </div>
                  </div>

                  <div className="form-group">
                    <label>PK(Primary Key)</label>
                    <span className="form-separator">:</span>
                    <input 
                      type="text" 
                      placeholder="Enter the Primary Key"
                      value={currentColumn.primaryKey ? 'Yes' : ''}
                      onChange={(e) => setCurrentColumn({...currentColumn, primaryKey: e.target.value === 'Yes'})}
                      className="form-control"
                    />
                  </div>

                  <div className="form-group">
                    <label>Default Values</label>
                    <span className="form-separator">:</span>
                    <input 
                      type="text" 
                      placeholder="Enter the Default value"
                      value={currentColumn.defaultValue}
                      onChange={(e) => setCurrentColumn({...currentColumn, defaultValue: e.target.value})}
                      className="form-control"
                    />
                  </div>

                  <div className="form-group">
                    <label>Is User Updatable ?</label>
                    <span className="form-separator">:</span>
                    <div className="checkbox-group">
                      <label className="checkbox-label">
                        <input 
                          type="radio" 
                          name="isUserUpdatable"
                          checked={currentColumn.isUserUpdatable === true}
                          onChange={() => setCurrentColumn({...currentColumn, isUserUpdatable: true})}
                        />
                        Yes
                      </label>
                      <label className="checkbox-label">
                        <input 
                          type="radio" 
                          name="isUserUpdatable"
                          checked={currentColumn.isUserUpdatable === false}
                          onChange={() => setCurrentColumn({...currentColumn, isUserUpdatable: false})}
                        />
                        No
                      </label>
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Is DG Enabled ?</label>
                    <span className="form-separator">:</span>
                    <div className="checkbox-group">
                      <label className="checkbox-label">
                        <input 
                          type="radio" 
                          name="isDGEnabled"
                          checked={currentColumn.isDGEnabled === true}
                          onChange={() => setCurrentColumn({...currentColumn, isDGEnabled: true})}
                        />
                        Yes
                      </label>
                      <label className="checkbox-label">
                        <input 
                          type="radio" 
                          name="isDGEnabled"
                          checked={currentColumn.isDGEnabled === false}
                          onChange={() => setCurrentColumn({...currentColumn, isDGEnabled: false})}
                        />
                        No
                      </label>
                    </div>
                  </div>
                </div>

                {/* Right Side - Added Columns */}
                <div className="columns-section">
                  <h3 className="columns-title">{tableName || 'Table Name'}</h3>
                  
                  <div className="columns-list">
                    {columns.length === 0 ? (
                      <div className="no-columns">No columns added yet</div>
                    ) : (
                      columns.map((col, idx) => (
                        <div key={idx} className="column-item">
                          <input 
                            type="text" 
                            value={col.columnName}
                            readOnly
                            className="form-control"
                          />
                        </div>
                      ))
                    )}
                  </div>

                  <button 
                    className="btn btn-primary btn-add-column"
                    onClick={handleAddColumn}
                  >
                    + Add Column
                  </button>
                </div>
              </div>

              {/* Form Buttons */}
              <div className="form-buttons">
                <button 
                  className="btn btn-success btn-submit"
                  onClick={handleSubmit}
                >
                  Submit
                </button>
                <button 
                  className="btn btn-secondary btn-cancel"
                  onClick={handleCancel}
                >
                  Cancel
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
