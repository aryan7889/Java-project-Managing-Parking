import React, { useState, useEffect } from 'react';
import {
  Car,
  Bike,
  Truck,
  Ticket,
  LogOut,
  LogIn,
  Search,
  LayoutGrid,
  CheckCircle2,
  AlertTriangle,
  History,
  BarChart3,
  Terminal,
  RefreshCw,
  X,
  CreditCard,
  Printer,
  ChevronRight,
  ShieldAlert
} from 'lucide-react';

const RATES = {
  BIKE: 10,
  CAR: 20,
  TRUCK: 40
};

const INITIAL_SLOTS = [
  ...Array.from({ length: 5 }, (_, i) => ({ id: `B-${i + 1}`, type: 'BIKE', occupied: false, vehicle: null, ticketId: null })),
  ...Array.from({ length: 10 }, (_, i) => ({ id: `C-${i + 1}`, type: 'CAR', occupied: false, vehicle: null, ticketId: null })),
  ...Array.from({ length: 3 }, (_, i) => ({ id: `T-${i + 1}`, type: 'TRUCK', occupied: false, vehicle: null, ticketId: null })),
];

export default function App() {
  const [activeTab, setActiveTab] = useState('1'); // Matches 1-10 menu
  const [slots, setSlots] = useState(() => {
    const saved = localStorage.getItem('smart_parking_react_slots');
    return saved ? JSON.parse(saved) : INITIAL_SLOTS;
  });
  const [activeTickets, setActiveTickets] = useState(() => {
    const saved = localStorage.getItem('smart_parking_react_active_tickets');
    return saved ? JSON.parse(saved) : {};
  });
  const [history, setHistory] = useState(() => {
    const saved = localStorage.getItem('smart_parking_react_history');
    return saved ? JSON.parse(saved) : [];
  });
  const [ticketCounter, setTicketCounter] = useState(() => {
    const saved = localStorage.getItem('smart_parking_react_counter');
    return saved ? parseInt(saved) : 1001;
  });

  // Entry Form State
  const [entryType, setEntryType] = useState('CAR');
  const [entryReg, setEntryReg] = useState('');
  const [entryOwner, setEntryOwner] = useState('');

  // Exit Form State
  const [exitIdentifier, setExitIdentifier] = useState('');
  const [exitMinutes, setExitMinutes] = useState(70);

  // Search State
  const [searchQuery, setSearchQuery] = useState('');

  // Modals
  const [currentPass, setCurrentPass] = useState(null);
  const [currentInvoice, setCurrentInvoice] = useState(null);
  const [notification, setNotification] = useState(null);

  // Interactive Terminal State
  const [terminalLogs, setTerminalLogs] = useState([
    'Smart Parking System Terminal v2.0 [Java OOP Engine]',
    'Type 1-10 or menu options to navigate, or "help" for command list.',
    'Ready for input.'
  ]);
  const [terminalInput, setTerminalInput] = useState('');

  // Save State
  useEffect(() => {
    localStorage.setItem('smart_parking_react_slots', JSON.stringify(slots));
    localStorage.setItem('smart_parking_react_active_tickets', JSON.stringify(activeTickets));
    localStorage.setItem('smart_parking_react_history', JSON.stringify(history));
    localStorage.setItem('smart_parking_react_counter', ticketCounter.toString());
  }, [slots, activeTickets, history, ticketCounter]);

  const showNotification = (message, type = 'success') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 4000);
  };

  const handleEntry = (e) => {
    if (e) e.preventDefault();
    const reg = entryReg.trim().toUpperCase();
    const owner = entryOwner.trim();

    if (!reg || !owner) {
      showNotification('Registration number and owner name are required.', 'error');
      return;
    }

    // Check duplicate
    const isDuplicate = slots.some(s => s.occupied && s.vehicle && s.vehicle.reg === reg);
    if (isDuplicate) {
      showNotification(`DuplicateVehicleException: Vehicle [${reg}] is already active in parking lot!`, 'error');
      return;
    }

    // Find vacant slot
    const slotIndex = slots.findIndex(s => s.type === entryType && !s.occupied);
    if (slotIndex === -1) {
      showNotification(`SlotUnavailableException: No vacant slots for ${entryType}. Lot is full!`, 'error');
      return;
    }

    const ticketId = `TCK-${ticketCounter}`;
    setTicketCounter(prev => prev + 1);

    const now = new Date();
    const formattedDate = now.toLocaleString();

    const newVehicle = { reg, owner, type: entryType };
    const updatedSlots = [...slots];
    updatedSlots[slotIndex] = {
      ...updatedSlots[slotIndex],
      occupied: true,
      vehicle: newVehicle,
      ticketId
    };

    const newTicket = {
      ticketId,
      vehicle: newVehicle,
      slotId: updatedSlots[slotIndex].id,
      entryTime: formattedDate,
      status: 'ACTIVE'
    };

    setSlots(updatedSlots);
    setActiveTickets(prev => ({ ...prev, [ticketId]: newTicket }));

    setEntryReg('');
    setEntryOwner('');
    setCurrentPass(newTicket);
    showNotification(`Vehicle [${reg}] parked in slot ${updatedSlots[slotIndex].id}`);
  };

  const handleExit = (e) => {
    if (e) e.preventDefault();
    const query = exitIdentifier.trim().toUpperCase();
    if (!query) {
      showNotification('Please enter a ticket ID or vehicle registration number.', 'error');
      return;
    }

    // Find ticket
    let ticket = activeTickets[query];
    if (!ticket) {
      ticket = Object.values(activeTickets).find(t => t.vehicle.reg === query);
    }

    if (!ticket) {
      showNotification(`InvalidTicketException: No active session found for [${query}]`, 'error');
      return;
    }

    // Apply Duration Ceiling Rule
    const minutes = parseInt(exitMinutes) || 60;
    const billedHours = minutes <= 0 ? 1 : Math.ceil(minutes / 60.0);
    const hourlyRate = RATES[ticket.vehicle.type];
    const feeCharged = billedHours * hourlyRate;
    const exitTime = new Date().toLocaleString();

    // Release slot
    const updatedSlots = slots.map(s => {
      if (s.id === ticket.slotId) {
        return { ...s, occupied: false, vehicle: null, ticketId: null };
      }
      return s;
    });

    const updatedActive = { ...activeTickets };
    delete updatedActive[ticket.ticketId];

    const completedTicket = {
      ...ticket,
      exitTime,
      durationMinutes: minutes,
      durationHours: billedHours,
      feeCharged,
      hourlyRate,
      status: 'COMPLETED'
    };

    setSlots(updatedSlots);
    setActiveTickets(updatedActive);
    setHistory(prev => [completedTicket, ...prev]);

    setExitIdentifier('');
    setCurrentInvoice(completedTicket);
    showNotification(`Vehicle [${ticket.vehicle.reg}] checked out. Fee: ₹${feeCharged.toFixed(2)}`);
  };

  const handleResetData = () => {
    if (window.confirm('Reset all demo data back to clean initial state?')) {
      setSlots(INITIAL_SLOTS);
      setActiveTickets({});
      setHistory([]);
      setTicketCounter(1001);
      showNotification('Demo data reset successfully.');
    }
  };

  // Terminal command processor
  const handleTerminalSubmit = (e) => {
    e.preventDefault();
    const cmd = terminalInput.trim();
    if (!cmd) return;

    const newLogs = [...terminalLogs, `> ${cmd}`];

    switch (cmd.toLowerCase()) {
      case '1':
      case 'entry':
        setActiveTab('1');
        newLogs.push('Switched to [1. Register Vehicle / Entry]');
        break;
      case '2':
      case 'exit':
        setActiveTab('2');
        newLogs.push('Switched to [2. Vehicle Exit & Billing]');
        break;
      case '3':
      case 'slots':
        setActiveTab('3');
        newLogs.push('Switched to [3. Display All Parking Slots]');
        break;
      case '4':
      case 'available':
        setActiveTab('4');
        newLogs.push('Switched to [4. Display Available Slots]');
        break;
      case '5':
      case 'occupied':
        setActiveTab('5');
        newLogs.push('Switched to [5. Display Occupied Slots]');
        break;
      case '6':
      case 'search':
        setActiveTab('6');
        newLogs.push('Switched to [6. Search Vehicle]');
        break;
      case '7':
        setActiveTab('7');
        newLogs.push('Switched to [7. Search Parking Ticket]');
        break;
      case '8':
      case 'history':
        setActiveTab('8');
        newLogs.push('Switched to [8. View Parking History]');
        break;
      case '9':
      case 'stats':
        setActiveTab('9');
        newLogs.push('Switched to [9. Display Parking Statistics]');
        break;
      case '10':
        newLogs.push('Simulated Exit: State synchronized with persistent storage.');
        break;
      case 'clear':
        setTerminalLogs(['Console cleared. Ready for input.']);
        setTerminalInput('');
        return;
      case 'help':
        newLogs.push(
          'Available commands:',
          ' 1-10: Select menu option',
          ' status: Show occupancy summary',
          ' clear: Clear terminal output'
        );
        break;
      case 'status':
        const occ = slots.filter(s => s.occupied).length;
        newLogs.push(`Total Slots: 18 | Occupied: ${occ} | Available: ${18 - occ} | Active Tickets: ${Object.keys(activeTickets).length}`);
        break;
      default:
        newLogs.push(`Command not recognized: "${cmd}". Type "help" or enter 1-10.`);
    }

    setTerminalLogs(newLogs);
    setTerminalInput('');
  };

  const totalSlots = slots.length;
  const occupiedSlots = slots.filter(s => s.occupied).length;
  const availableSlots = totalSlots - occupiedSlots;
  const totalRevenue = history.reduce((sum, h) => sum + h.feeCharged, 0);

  const menuOptions = [
    { id: '1', title: 'Register Vehicle / Entry', icon: LogIn, badge: 'New' },
    { id: '2', title: 'Vehicle Exit & Billing', icon: LogOut, badge: 'Checkout' },
    { id: '3', title: 'Display All Slots', icon: LayoutGrid, count: totalSlots },
    { id: '4', title: 'Display Available Slots', icon: CheckCircle2, count: availableSlots },
    { id: '5', title: 'Display Occupied Slots', icon: AlertTriangle, count: occupiedSlots },
    { id: '6', title: 'Search Vehicle', icon: Search },
    { id: '7', title: 'Search Parking Ticket', icon: Ticket },
    { id: '8', title: 'View Parking History', icon: History, count: history.length },
    { id: '9', title: 'Parking Statistics', icon: BarChart3 },
    { id: '10', title: 'Interactive Terminal CLI', icon: Terminal, badge: 'CLI' },
  ];

  return (
    <div class="min-h-screen bg-slate-950 text-slate-100 flex flex-col">
      {/* Toast Notification */}
      {notification && (
        <div className={`fixed top-4 right-4 z-50 flex items-center gap-2 px-4 py-3 rounded-xl border shadow-2xl transition-all duration-300 ${
          notification.type === 'error'
            ? 'bg-rose-950/90 border-rose-500 text-rose-200'
            : 'bg-emerald-950/90 border-emerald-500 text-emerald-200'
        }`}>
          {notification.type === 'error' ? <ShieldAlert className="w-5 h-5" /> : <CheckCircle2 className="w-5 h-5" />}
          <span className="text-sm font-medium">{notification.message}</span>
        </div>
      )}

      {/* Top Navbar */}
      <header className="bg-slate-900 border-b border-slate-800 sticky top-0 z-40 px-4 sm:px-6 py-3.5 flex items-center justify-between shadow-lg">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-blue-600/20 text-blue-400 border border-blue-500/30 rounded-xl">
            <Car className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-lg sm:text-xl font-bold tracking-tight text-white flex items-center gap-2">
              Smart Parking Management System
              <span className="text-[11px] font-semibold bg-blue-500/20 text-blue-400 border border-blue-500/30 px-2 py-0.5 rounded-full">
                React + Java OOP
              </span>
            </h1>
            <p className="text-xs text-slate-400">Interactive Menu-Driven Web Interface</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleResetData}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-300 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg transition"
            title="Reset storage to initial state"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Reset State</span>
          </button>
        </div>
      </header>

      {/* Main Body with Sidebar Menu */}
      <div className="flex-1 flex flex-col md:flex-row max-w-7xl w-full mx-auto p-4 sm:p-6 gap-6">

        {/* Sidebar Menu Navigation (1-10 Options) */}
        <aside className="w-full md:w-72 bg-slate-900 border border-slate-800 rounded-2xl p-4 flex flex-col shrink-0 shadow-xl h-fit">
          <div className="pb-3 mb-2 border-b border-slate-800 flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Application Menu</span>
            <span className="text-[11px] font-mono text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded">10 Options</span>
          </div>

          <nav className="space-y-1">
            {menuOptions.map((item) => {
              const Icon = item.icon;
              const isActive = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => setActiveTab(item.id)}
                  className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl text-xs sm:text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-blue-600 text-white shadow-md shadow-blue-900/40'
                      : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                  }`}
                >
                  <div className="flex items-center gap-2.5">
                    <span className={`w-5 text-center font-mono font-bold text-xs ${isActive ? 'text-blue-200' : 'text-slate-500'}`}>
                      {item.id}
                    </span>
                    <Icon className="w-4 h-4 shrink-0" />
                    <span className="truncate">{item.title}</span>
                  </div>

                  {item.count !== undefined && (
                    <span className={`text-[11px] px-2 py-0.5 rounded-full font-semibold ${
                      isActive ? 'bg-blue-700 text-blue-100' : 'bg-slate-800 text-slate-400'
                    }`}>
                      {item.count}
                    </span>
                  )}
                  {item.badge && (
                    <span className="text-[10px] px-1.5 py-0.5 rounded uppercase font-bold bg-amber-500/20 text-amber-300">
                      {item.badge}
                    </span>
                  )}
                </button>
              );
            })}
          </nav>

          {/* Quick Metrics Badge in Sidebar */}
          <div className="mt-6 pt-4 border-t border-slate-800/80 space-y-2 text-xs">
            <div className="flex justify-between text-slate-400">
              <span>Occupancy:</span>
              <span className="text-emerald-400 font-semibold">{Math.round((occupiedSlots / totalSlots) * 100)}%</span>
            </div>
            <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
              <div
                className="bg-emerald-500 h-1.5 transition-all duration-500"
                style={{ width: `${(occupiedSlots / totalSlots) * 100}%` }}
              />
            </div>
            <div className="flex justify-between text-slate-400 pt-1">
              <span>Total Revenue:</span>
              <span className="text-white font-bold">₹{totalRevenue.toFixed(2)}</span>
            </div>
          </div>
        </aside>

        {/* Content Area for Current Active Menu Option */}
        <main className="flex-1 space-y-6">

          {/* Top Quick Stats Strip */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4">
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
              <span className="text-xs text-slate-400">Total Slots</span>
              <p className="text-2xl font-bold text-white mt-1">{totalSlots}</p>
            </div>
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
              <span className="text-xs text-emerald-400">Available</span>
              <p className="text-2xl font-bold text-emerald-400 mt-1">{availableSlots}</p>
            </div>
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
              <span className="text-xs text-amber-400">Occupied</span>
              <p className="text-2xl font-bold text-amber-400 mt-1">{occupiedSlots}</p>
            </div>
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
              <span className="text-xs text-blue-400">Revenue</span>
              <p className="text-2xl font-bold text-blue-400 mt-1">₹{totalRevenue.toFixed(0)}</p>
            </div>
          </div>

          {/* Option 1: Vehicle Entry */}
          {activeTab === '1' && (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-6">
              <div className="flex items-center gap-3 border-b border-slate-800 pb-4">
                <div className="p-2.5 bg-emerald-500/20 text-emerald-400 rounded-xl">
                  <LogIn className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-white">[Option 1] Register Vehicle / Vehicle Entry</h2>
                  <p className="text-xs text-slate-400">Allocate suitable slot, record arrival timestamp, and generate parking ticket.</p>
                </div>
              </div>

              <form onSubmit={handleEntry} className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Vehicle Type</label>
                  <div className="grid grid-cols-3 gap-2">
                    {[
                      { type: 'BIKE', name: 'Bike', rate: '₹10/hr', icon: Bike },
                      { type: 'CAR', name: 'Car', rate: '₹20/hr', icon: Car },
                      { type: 'TRUCK', name: 'Truck', rate: '₹40/hr', icon: Truck },
                    ].map(item => {
                      const Icon = item.icon;
                      const selected = entryType === item.type;
                      return (
                        <button
                          type="button"
                          key={item.type}
                          onClick={() => setEntryType(item.type)}
                          className={`p-3 rounded-xl border text-center transition flex flex-col items-center gap-1 ${
                            selected
                              ? 'bg-blue-600/20 border-blue-500 text-blue-300'
                              : 'bg-slate-950 border-slate-800 text-slate-400 hover:border-slate-700'
                          }`}
                        >
                          <Icon className="w-5 h-5" />
                          <span className="text-xs font-bold">{item.name}</span>
                          <span className="text-[10px] text-slate-500">{item.rate}</span>
                        </button>
                      );
                    })}
                  </div>
                </div>

                <div className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-300 mb-1">Registration Plate Number</label>
                    <input
                      type="text"
                      required
                      value={entryReg}
                      onChange={(e) => setEntryReg(e.target.value)}
                      placeholder="e.g. MH-12-AB-1234"
                      className="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-sm uppercase focus:outline-none focus:border-blue-500 placeholder:text-slate-600"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-300 mb-1">Owner Name</label>
                    <input
                      type="text"
                      required
                      value={entryOwner}
                      onChange={(e) => setEntryOwner(e.target.value)}
                      placeholder="e.g. Rahul Sharma"
                      className="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-blue-500 placeholder:text-slate-600"
                    />
                  </div>
                </div>

                <div className="md:col-span-2 pt-2">
                  <button
                    type="submit"
                    className="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-semibold py-3 px-6 rounded-xl text-sm transition flex items-center justify-center gap-2 shadow-lg shadow-emerald-900/30"
                  >
                    <Ticket className="w-4 h-4" />
                    <span>Park Vehicle & Issue Entry Pass</span>
                  </button>
                </div>
              </form>
            </div>
          )}

          {/* Option 2: Vehicle Exit */}
          {activeTab === '2' && (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-6">
              <div className="flex items-center gap-3 border-b border-slate-800 pb-4">
                <div className="p-2.5 bg-blue-500/20 text-blue-400 rounded-xl">
                  <LogOut className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-white">[Option 2] Vehicle Exit & Billing</h2>
                  <p className="text-xs text-slate-400">Compute duration ceiling, calculate polymorphic fee, release slot, and print invoice.</p>
                </div>
              </div>

              <form onSubmit={handleExit} className="space-y-4 max-w-xl">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Ticket ID or Vehicle Registration Number
                  </label>
                  <input
                    type="text"
                    required
                    value={exitIdentifier}
                    onChange={(e) => setExitIdentifier(e.target.value)}
                    placeholder="e.g. TCK-1001 or MH-12-AB-1234"
                    className="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-sm uppercase focus:outline-none focus:border-blue-500 placeholder:text-slate-600"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Simulated Parked Duration (Test Ceiling Rule)
                  </label>
                  <select
                    value={exitMinutes}
                    onChange={(e) => setExitMinutes(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-blue-500"
                  >
                    <option value="25">25 minutes (Billed as 1 hour - Ceiling rule)</option>
                    <option value="60">60 minutes (Billed as 1 hour)</option>
                    <option value="70">1 hr 10 mins / 70 mins (Billed as 2 hours - Ceiling rule)</option>
                    <option value="181">3 hrs 1 min / 181 mins (Billed as 4 hours - Ceiling rule)</option>
                    <option value="300">5 hours (Billed as 5 hours)</option>
                  </select>
                </div>

                <button
                  type="submit"
                  className="w-full bg-blue-600 hover:bg-blue-500 text-white font-semibold py-3 px-6 rounded-xl text-sm transition flex items-center justify-center gap-2 shadow-lg shadow-blue-900/30"
                >
                  <CreditCard className="w-4 h-4" />
                  <span>Process Checkout & Release Slot</span>
                </button>
              </form>
            </div>
          )}

          {/* Options 3, 4, 5: Slots Views (All, Available, Occupied) */}
          {(activeTab === '3' || activeTab === '4' || activeTab === '5') && (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-6">
              <div className="flex items-center justify-between border-b border-slate-800 pb-4">
                <div>
                  <h2 className="text-lg font-bold text-white">
                    {activeTab === '3' && '[Option 3] All Parking Slots (18 Total)'}
                    {activeTab === '4' && '[Option 4] Available (Vacant) Slots'}
                    {activeTab === '5' && '[Option 5] Occupied Parking Slots'}
                  </h2>
                  <p className="text-xs text-slate-400">Click any occupied slot to pre-fill the checkout form.</p>
                </div>
                <div className="flex gap-2">
                  <button onClick={() => setActiveTab('3')} className={`px-2.5 py-1 text-xs rounded-lg ${activeTab === '3' ? 'bg-blue-600 text-white' : 'bg-slate-800 text-slate-300'}`}>All</button>
                  <button onClick={() => setActiveTab('4')} className={`px-2.5 py-1 text-xs rounded-lg ${activeTab === '4' ? 'bg-emerald-600 text-white' : 'bg-slate-800 text-slate-300'}`}>Vacant</button>
                  <button onClick={() => setActiveTab('5')} className={`px-2.5 py-1 text-xs rounded-lg ${activeTab === '5' ? 'bg-rose-600 text-white' : 'bg-slate-800 text-slate-300'}`}>Occupied</button>
                </div>
              </div>

              {/* 2D Grid Visualizer */}
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-3">
                {slots
                  .filter(s => {
                    if (activeTab === '4') return !s.occupied;
                    if (activeTab === '5') return s.occupied;
                    return true;
                  })
                  .map(slot => {
                    const isOccupied = slot.occupied;
                    const Icon = slot.type === 'BIKE' ? Bike : slot.type === 'CAR' ? Car : Truck;
                    return (
                      <div
                        key={slot.id}
                        onClick={() => {
                          if (isOccupied && slot.ticketId) {
                            setExitIdentifier(slot.ticketId);
                            setActiveTab('2');
                          }
                        }}
                        className={`rounded-xl border p-3 flex flex-col justify-between h-28 transition-all ${
                          isOccupied
                            ? 'bg-rose-950/30 border-rose-500/50 hover:bg-rose-900/40 cursor-pointer shadow-md shadow-rose-950/40 slot-pulse'
                            : 'bg-emerald-950/20 border-emerald-500/30 hover:bg-emerald-950/40'
                        }`}
                      >
                        <div className="flex items-center justify-between text-xs font-bold">
                          <span className={isOccupied ? 'text-rose-400' : 'text-emerald-400'}>{slot.id}</span>
                          <Icon className={`w-4 h-4 ${isOccupied ? 'text-rose-400' : 'text-emerald-400'}`} />
                        </div>

                        {isOccupied ? (
                          <div className="space-y-0.5">
                            <p className="text-xs font-extrabold text-white truncate">{slot.vehicle.reg}</p>
                            <p className="text-[11px] text-slate-400 truncate">{slot.vehicle.owner}</p>
                          </div>
                        ) : (
                          <span className="text-[11px] font-semibold text-emerald-400/90 self-start bg-emerald-500/10 px-1.5 py-0.5 rounded">
                            VACANT
                          </span>
                        )}

                        <span className="text-[10px] text-slate-500 uppercase">{slot.type}</span>
                      </div>
                    );
                  })}
              </div>
            </div>
          )}

          {/* Option 6 & 7: Search (Vehicle or Ticket) */}
          {(activeTab === '6' || activeTab === '7') && (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-6">
              <div className="flex items-center gap-3 border-b border-slate-800 pb-4">
                <div className="p-2.5 bg-purple-500/20 text-purple-400 rounded-xl">
                  <Search className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-white">
                    {activeTab === '6' ? '[Option 6] Search Vehicle' : '[Option 7] Search Parking Ticket'}
                  </h2>
                  <p className="text-xs text-slate-400">Search active slots and completed historical records.</p>
                </div>
              </div>

              <div className="max-w-md">
                <div className="relative">
                  <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3.5" />
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder={activeTab === '6' ? 'Enter license plate (e.g. MH-12)' : 'Enter ticket ID (e.g. TCK-1001)'}
                    className="w-full bg-slate-950 border border-slate-700 rounded-xl pl-10 pr-4 py-2.5 text-sm uppercase focus:outline-none focus:border-blue-500 placeholder:text-slate-600"
                  />
                </div>
              </div>

              {/* Search Results */}
              <div className="space-y-3">
                <h3 className="text-xs font-semibold uppercase text-slate-400">Results</h3>

                {/* Active matching */}
                {Object.values(activeTickets)
                  .filter(t => {
                    const q = searchQuery.trim().toUpperCase();
                    if (!q) return false;
                    return activeTab === '6' ? t.vehicle.reg.includes(q) : t.ticketId.includes(q);
                  })
                  .map(t => (
                    <div key={t.ticketId} className="p-4 rounded-xl bg-slate-950 border border-slate-800 flex justify-between items-center">
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="font-mono text-xs font-bold text-blue-400">{t.ticketId}</span>
                          <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400">ACTIVE IN LOT</span>
                        </div>
                        <p className="text-sm font-bold text-white mt-1">{t.vehicle.reg} ({t.vehicle.type})</p>
                        <p className="text-xs text-slate-400">Owner: {t.vehicle.owner} • Slot: {t.slotId}</p>
                      </div>
                      <button
                        onClick={() => {
                          setExitIdentifier(t.ticketId);
                          setActiveTab('2');
                        }}
                        className="px-3 py-1.5 text-xs bg-blue-600 hover:bg-blue-500 text-white rounded-lg"
                      >
                        Checkout
                      </button>
                    </div>
                  ))}

                {/* History matching */}
                {history
                  .filter(h => {
                    const q = searchQuery.trim().toUpperCase();
                    if (!q) return false;
                    return activeTab === '6' ? h.vehicle.reg.includes(q) : h.ticketId.includes(q);
                  })
                  .map(h => (
                    <div key={h.ticketId} className="p-4 rounded-xl bg-slate-950 border border-slate-800 flex justify-between items-center">
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="font-mono text-xs font-bold text-slate-400">{h.ticketId}</span>
                          <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-slate-800 text-slate-300">COMPLETED</span>
                        </div>
                        <p className="text-sm font-bold text-white mt-1">{h.vehicle.reg} ({h.vehicle.type})</p>
                        <p className="text-xs text-slate-400">Duration: {h.durationHours} hr(s) • Paid: ₹{h.feeCharged.toFixed(2)}</p>
                      </div>
                    </div>
                  ))}

                {!searchQuery && (
                  <p className="text-xs text-slate-500 italic">Type a registration plate or ticket ID above to search.</p>
                )}
              </div>
            </div>
          )}

          {/* Option 8: Parking History */}
          {activeTab === '8' && (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-6">
              <div className="flex items-center justify-between border-b border-slate-800 pb-4">
                <div className="flex items-center gap-3">
                  <div className="p-2.5 bg-amber-500/20 text-amber-400 rounded-xl">
                    <History className="w-5 h-5" />
                  </div>
                  <div>
                    <h2 className="text-lg font-bold text-white">[Option 8] View Parking History</h2>
                    <p className="text-xs text-slate-400">Completed parking sessions and billed amounts.</p>
                  </div>
                </div>
                <span className="text-xs font-bold bg-slate-800 px-3 py-1 rounded-lg text-slate-300">
                  {history.length} Completed Sessions
                </span>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm text-slate-300">
                  <thead className="bg-slate-950/60 text-xs font-semibold text-slate-400 uppercase tracking-wider border-b border-slate-800">
                    <tr>
                      <th className="px-4 py-3">Ticket ID</th>
                      <th className="px-4 py-3">Vehicle Plate</th>
                      <th className="px-4 py-3">Type</th>
                      <th className="px-4 py-3">Slot</th>
                      <th className="px-4 py-3">Duration (Ceiling)</th>
                      <th className="px-4 py-3">Fee Paid</th>
                      <th className="px-4 py-3">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60">
                    {history.length === 0 ? (
                      <tr>
                        <td colSpan="7" className="px-4 py-8 text-center text-slate-500 italic">
                          No completed sessions yet.
                        </td>
                      </tr>
                    ) : (
                      history.map(item => (
                        <tr key={item.ticketId} className="hover:bg-slate-800/40">
                          <td className="px-4 py-3 font-mono font-bold text-blue-400 text-xs">{item.ticketId}</td>
                          <td className="px-4 py-3 font-semibold text-white text-xs">{item.vehicle.reg}</td>
                          <td className="px-4 py-3 text-xs">{item.vehicle.type}</td>
                          <td className="px-4 py-3 font-mono text-emerald-400 text-xs">{item.slotId}</td>
                          <td className="px-4 py-3 text-xs">{item.durationHours} hr(s)</td>
                          <td className="px-4 py-3 font-bold text-emerald-400 text-xs">₹{item.feeCharged.toFixed(2)}</td>
                          <td className="px-4 py-3 text-xs">
                            <span className="px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 text-[11px] font-semibold border border-emerald-500/20">
                              PAID
                            </span>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Option 9: Statistics */}
          {activeTab === '9' && (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-6">
              <div className="flex items-center gap-3 border-b border-slate-800 pb-4">
                <div className="p-2.5 bg-blue-500/20 text-blue-400 rounded-xl">
                  <BarChart3 className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-white">[Option 9] Parking Facility Statistics</h2>
                  <p className="text-xs text-slate-400">Real-time capacity, category breakdown, and revenue analytics.</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-slate-950 border border-slate-800 rounded-xl p-5">
                  <span className="text-xs font-semibold text-amber-400">Bike Zone Capacity</span>
                  <p className="text-xl font-bold text-white mt-1">5 Slots</p>
                  <p className="text-xs text-slate-400 mt-1">
                    {slots.filter(s => s.type === 'BIKE' && !s.occupied).length} Available • ₹10/hr
                  </p>
                </div>
                <div className="bg-slate-950 border border-slate-800 rounded-xl p-5">
                  <span className="text-xs font-semibold text-blue-400">Car Zone Capacity</span>
                  <p className="text-xl font-bold text-white mt-1">10 Slots</p>
                  <p className="text-xs text-slate-400 mt-1">
                    {slots.filter(s => s.type === 'CAR' && !s.occupied).length} Available • ₹20/hr
                  </p>
                </div>
                <div className="bg-slate-950 border border-slate-800 rounded-xl p-5">
                  <span className="text-xs font-semibold text-purple-400">Truck Zone Capacity</span>
                  <p className="text-xl font-bold text-white mt-1">3 Slots</p>
                  <p className="text-xs text-slate-400 mt-1">
                    {slots.filter(s => s.type === 'TRUCK' && !s.occupied).length} Available • ₹40/hr
                  </p>
                </div>
              </div>

              <div className="bg-slate-950 border border-slate-800 rounded-xl p-5 space-y-3">
                <div className="flex justify-between items-center text-sm font-semibold">
                  <span>Occupancy Rate</span>
                  <span className="text-emerald-400">{Math.round((occupiedSlots / totalSlots) * 100)}%</span>
                </div>
                <div className="w-full bg-slate-800 rounded-full h-3 overflow-hidden">
                  <div
                    className="bg-emerald-500 h-3 transition-all duration-500"
                    style={{ width: `${(occupiedSlots / totalSlots) * 100}%` }}
                  />
                </div>
                <div className="flex justify-between text-xs text-slate-400 pt-1">
                  <span>{occupiedSlots} Occupied</span>
                  <span>{availableSlots} Available</span>
                </div>
              </div>
            </div>
          )}

          {/* Option 10: Interactive Terminal CLI */}
          {activeTab === '10' && (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
              <div className="flex items-center justify-between border-b border-slate-800 pb-4">
                <div className="flex items-center gap-3">
                  <div className="p-2.5 bg-emerald-500/20 text-emerald-400 rounded-xl">
                    <Terminal className="w-5 h-5" />
                  </div>
                  <div>
                    <h2 className="text-lg font-bold text-white">[Option 10] Interactive Terminal Console</h2>
                    <p className="text-xs text-slate-400">Directly enter menu numbers (1-10) to control the application via terminal commands.</p>
                  </div>
                </div>
                <button
                  onClick={() => setTerminalLogs(['Console cleared. Ready for input.'])}
                  className="px-3 py-1 text-xs bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg"
                >
                  Clear Console
                </button>
              </div>

              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 font-mono text-xs text-emerald-400 h-72 overflow-y-auto space-y-1">
                {terminalLogs.map((log, index) => (
                  <p key={index} className={log.startsWith('>') ? 'text-white font-bold' : ''}>{log}</p>
                ))}
              </div>

              <form onSubmit={handleTerminalSubmit} className="flex gap-2">
                <div className="relative flex-1">
                  <span className="absolute left-3 top-2.5 text-slate-500 font-mono">&gt;</span>
                  <input
                    type="text"
                    value={terminalInput}
                    onChange={(e) => setTerminalInput(e.target.value)}
                    placeholder="Enter menu option (1-10), 'status', 'help'..."
                    className="w-full bg-slate-950 border border-slate-700 rounded-xl pl-8 pr-4 py-2.5 text-xs font-mono text-white focus:outline-none focus:border-emerald-500"
                  />
                </div>
                <button
                  type="submit"
                  className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-semibold transition"
                >
                  Send
                </button>
              </form>
            </div>
          )}

        </main>
      </div>

      {/* Entry Pass Modal */}
      {currentPass && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-sm w-full p-6 shadow-2xl relative">
            <button onClick={() => setCurrentPass(null)} className="absolute top-4 right-4 text-slate-400 hover:text-white">
              <X className="w-5 h-5" />
            </button>
            <div className="text-center pb-4 border-b border-slate-800">
              <div className="inline-block p-2 bg-emerald-500/20 text-emerald-400 rounded-full mb-2">
                <Ticket className="w-6 h-6" />
              </div>
              <h3 className="text-base font-bold text-white">SMART PARKING ENTRY PASS</h3>
              <p className="text-xs text-slate-400">Keep this slip for checkout</p>
            </div>
            <div className="py-4 space-y-2 text-xs">
              <div className="flex justify-between"><span className="text-slate-400">Ticket ID:</span><span className="font-mono font-bold text-blue-400">{currentPass.ticketId}</span></div>
              <div className="flex justify-between"><span className="text-slate-400">Vehicle No:</span><span className="font-bold text-white">{currentPass.vehicle.reg}</span></div>
              <div className="flex justify-between"><span className="text-slate-400">Vehicle Type:</span><span>{currentPass.vehicle.type} (₹{RATES[currentPass.vehicle.type]}/hr)</span></div>
              <div className="flex justify-between"><span className="text-slate-400">Owner Name:</span><span>{currentPass.vehicle.owner}</span></div>
              <div className="flex justify-between"><span className="text-slate-400">Assigned Slot:</span><span className="font-bold text-emerald-400">{currentPass.slotId}</span></div>
              <div className="flex justify-between"><span className="text-slate-400">Entry Time:</span><span className="text-slate-300">{currentPass.entryTime}</span></div>
              <div className="flex justify-between"><span className="text-slate-400">Status:</span><span className="text-emerald-400 font-semibold">ACTIVE</span></div>
            </div>
            <button
              onClick={() => setCurrentPass(null)}
              className="w-full mt-2 bg-slate-800 hover:bg-slate-700 text-white font-semibold py-2.5 rounded-xl text-xs transition"
            >
              Done
            </button>
          </div>
        </div>
      )}

      {/* Exit Invoice Modal */}
      {currentInvoice && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-sm w-full p-6 shadow-2xl relative">
            <button onClick={() => setCurrentInvoice(null)} className="absolute top-4 right-4 text-slate-400 hover:text-white">
              <X className="w-5 h-5" />
            </button>
            <div className="text-center pb-4 border-b border-slate-800">
              <div className="inline-block p-2 bg-blue-500/20 text-blue-400 rounded-full mb-2">
                <CreditCard className="w-6 h-6" />
              </div>
              <h3 className="text-base font-bold text-white">PARKING EXIT INVOICE</h3>
              <p className="text-xs text-slate-400">Payment Completed • Slot Released</p>
            </div>
            <div className="py-4 space-y-2 text-xs">
              <div className="flex justify-between"><span className="text-slate-400">Ticket ID:</span><span className="font-mono font-bold text-blue-400">{currentInvoice.ticketId}</span></div>
              <div className="flex justify-between"><span className="text-slate-400">Vehicle No:</span><span className="font-bold text-white">{currentInvoice.vehicle.reg}</span></div>
              <div className="flex justify-between"><span className="text-slate-400">Released Slot:</span><span className="font-bold text-emerald-400">{currentInvoice.slotId} (VACANT)</span></div>
              <div className="flex justify-between"><span className="text-slate-400">Duration:</span><span>{currentInvoice.durationMinutes} mins</span></div>
              <div className="flex justify-between"><span className="text-slate-400">Billed Duration:</span><span className="font-bold text-white">{currentInvoice.durationHours} hr(s) <span className="text-amber-400 font-normal">(Ceiling)</span></span></div>
              <div className="flex justify-between"><span className="text-slate-400">Rate:</span><span>₹{currentInvoice.hourlyRate}/hr</span></div>
              <div className="flex justify-between pt-2 border-t border-slate-800 text-sm"><span className="font-bold text-white">TOTAL DUE:</span><span className="font-extrabold text-emerald-400">₹{currentInvoice.feeCharged.toFixed(2)}</span></div>
            </div>
            <button
              onClick={() => setCurrentInvoice(null)}
              className="w-full mt-2 bg-blue-600 hover:bg-blue-500 text-white font-semibold py-2.5 rounded-xl text-xs transition"
            >
              Close Receipt
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
