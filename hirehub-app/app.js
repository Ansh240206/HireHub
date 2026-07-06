const API_BASE = "";

// Helper to make API calls using native fetch with JWT authorization and refresh tokens.
async function apiRequest(url, options = {}) {
  const headers = new Headers(options.headers || {});
  
  if (options.body && !(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }
  
  const token = localStorage.getItem("hirehub_access_token");
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  
  const response = await fetch(`${API_BASE}${url}`, {
    ...options,
    headers
  });
  
  if (response.status === 401) {
    // Attempt token refresh
    const refreshToken = localStorage.getItem("hirehub_refresh_token");
    if (refreshToken && url !== "/api/auth/refresh" && url !== "/api/auth/login") {
      try {
        const refreshResponse = await fetch(`${API_BASE}/api/auth/refresh`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ refreshToken })
        });
        if (refreshResponse.ok) {
          const resData = await refreshResponse.json();
          localStorage.setItem("hirehub_access_token", resData.data.accessToken);
          localStorage.setItem("hirehub_refresh_token", resData.data.refreshToken);
          
          headers.set("Authorization", `Bearer ${resData.data.accessToken}`);
          const retryResponse = await fetch(`${API_BASE}${url}`, { ...options, headers });
          if (retryResponse.status === 204) return null;
          const retryData = await retryResponse.json();
          if (!retryResponse.ok) throw new Error(retryData.message || "Request failed");
          return retryData.data;
        }
      } catch (e) {
        console.error("Token refresh failed", e);
      }
    }
    
    // Clear credentials and logout if refresh fails
    localStorage.removeItem("hirehub_access_token");
    localStorage.removeItem("hirehub_refresh_token");
    localStorage.removeItem("hirehub_user_id");
    localStorage.removeItem("hirehub_user_name");
    localStorage.removeItem("hirehub_user_email");
    localStorage.removeItem("hirehub_user_role");
    setState({ authed: false, view: "dashboard" });
    throw new Error("Session expired. Please log in again.");
  }
  
  if (response.status === 204) {
    return null;
  }
  
  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || "Request failed");
  }
  return data.data;
}

const savedToken = localStorage.getItem("hirehub_access_token");
const savedRole = localStorage.getItem("hirehub_user_role") || "ROLE_APPLICANT";
const savedName = localStorage.getItem("hirehub_user_name") || "";
const savedEmail = localStorage.getItem("hirehub_user_email") || "";

const state = {
  authed: !!savedToken,
  mode: "login",
  role: savedRole,
  view: "dashboard",
  query: "",
  location: "All",
  employment: "All",
  sort: "Newest",
  toast: "",
  userName: savedName,
  userEmail: savedEmail,
  
  applicantProfileComplete: false,
  profile: null,
  savedJobs: new Set(),
  applications: [],
  jobs: [],
  users: [],
  companies: [],
  company: null,
  editingJob: null,
  adminStats: null
};

const navByRole = {
  ROLE_APPLICANT: [
    ["dashboard", "Dashboard"],
    ["jobs", "Jobs"],
    ["applications", "Applications"],
    ["profile", "Profile"]
  ],
  ROLE_RECRUITER: [
    ["dashboard", "Dashboard"],
    ["jobs", "My Jobs"],
    ["applicants", "Applicants"],
    ["company", "Company"]
  ],
  ROLE_ADMIN: [
    ["dashboard", "Dashboard"],
    ["users", "Users"],
    ["companies", "Companies"],
    ["jobs", "Jobs"]
  ]
};

const roleNames = {
  ROLE_APPLICANT: "Applicant",
  ROLE_RECRUITER: "Recruiter",
  ROLE_ADMIN: "Admin"
};

const icons = {
  dashboard: "◫",
  jobs: "◇",
  applications: "✓",
  profile: "◉",
  applicants: "◎",
  company: "▣",
  users: "♙",
  companies: "▤"
};

async function loadData() {
  if (!state.authed) return;
  try {
    if (state.role === "ROLE_APPLICANT") {
      const jobsPage = await apiRequest("/api/jobs?size=100");
      state.jobs = jobsPage.content || [];
      
      try {
        const savedPage = await apiRequest("/api/saved-jobs?size=100");
        state.savedJobs = new Set((savedPage.content || []).map(j => j.id));
      } catch (e) {
        state.savedJobs = new Set();
      }
      
      try {
        const appsPage = await apiRequest("/api/applications/me?size=100");
        state.applications = appsPage.content || [];
      } catch (e) {
        state.applications = [];
      }
      
      try {
        const profile = await apiRequest("/api/applicant-profile/me");
        state.profile = profile;
        state.applicantProfileComplete = profile && profile.complete;
      } catch (e) {
        state.profile = null;
        state.applicantProfileComplete = false;
      }
    } else if (state.role === "ROLE_RECRUITER") {
      const myJobsPage = await apiRequest("/api/jobs/me?size=100");
      state.jobs = myJobsPage.content || [];
      
      try {
        const appsPage = await apiRequest("/api/applications/recruiter?size=100");
        state.applications = appsPage.content || [];
      } catch (e) {
        state.applications = [];
      }
      
      try {
        const companiesPage = await apiRequest("/api/companies?size=100");
        const userId = parseInt(localStorage.getItem("hirehub_user_id"));
        const myCompany = (companiesPage.content || []).find(c => c.ownerUserId === userId);
        state.company = myCompany || null;
      } catch (e) {
        state.company = null;
      }
    } else if (state.role === "ROLE_ADMIN") {
      const jobsPage = await apiRequest("/api/jobs?size=100");
      state.jobs = jobsPage.content || [];
      
      const usersPage = await apiRequest("/api/users?size=100");
      state.users = usersPage.content || [];
      
      const companiesPage = await apiRequest("/api/companies?size=100");
      state.companies = companiesPage.content || [];
      
      try {
        state.adminStats = await apiRequest("/api/admin/dashboard");
      } catch (e) {
        state.adminStats = null;
      }
    }
    render();
  } catch (err) {
    notify(err.message || "Failed to sync data with server");
  }
}

async function searchJobs() {
  if (state.role !== "ROLE_APPLICANT" && state.role !== "ROLE_ADMIN") return;
  try {
    let url = "/api/jobs?size=100";
    if (state.query.trim()) {
      url += `&keyword=${encodeURIComponent(state.query.trim())}`;
    }
    if (state.location && state.location !== "All") {
      url += `&location=${encodeURIComponent(state.location)}`;
    }
    if (state.employment && state.employment !== "All") {
      const type = state.employment.toUpperCase().replace("-", "_");
      url += `&employmentType=${type}`;
    }
    const pageData = await apiRequest(url);
    state.jobs = pageData.content || [];
    render();
  } catch (err) {
    notify(err.message || "Failed to search jobs");
  }
}

function setState(next) {
  const authedChanged = next.authed !== undefined && next.authed !== state.authed;
  const viewChanged = next.view !== undefined && next.view !== state.view;
  const roleChanged = next.role !== undefined && next.role !== state.role;
  
  Object.assign(state, next);
  
  if (authedChanged || viewChanged || roleChanged) {
    loadData();
  } else {
    render();
  }
}

function notify(message) {
  state.toast = message;
  render();
  window.clearTimeout(notify.timer);
  notify.timer = window.setTimeout(() => {
    state.toast = "";
    render();
  }, 2400);
}

function filteredJobs() {
  const q = state.query.trim().toLowerCase();
  let jobs = state.jobs.filter(job => {
    const matchesQuery = !q || [job.title, job.companyName || "", job.description].some(value => value.toLowerCase().includes(q));
    const matchesLocation = state.location === "All" || job.location === state.location;
    
    // Map backend type (e.g. FULL_TIME) back to match standard label or keep enum format
    const matchesEmployment = state.employment === "All" || job.employmentType === state.employment || job.employmentType === state.employment.toUpperCase().replace("-", "_");
    return matchesQuery && matchesLocation && matchesEmployment;
  });

  if (state.role === "ROLE_RECRUITER" && state.view === "jobs") {
    // Already filtered by API "/api/jobs/me"
  }

  const sorters = {
    Newest: (a, b) => new Date(b.deadline) - new Date(a.deadline),
    Oldest: (a, b) => new Date(a.deadline) - new Date(b.deadline),
    "Highest Salary": (a, b) => b.salary - a.salary,
    "Lowest Salary": (a, b) => a.salary - b.salary,
    Alphabetical: (a, b) => a.title.localeCompare(b.title)
  };

  return jobs.sort(sorters[state.sort]);
}

function money(value) {
  return new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(value);
}

function statusClass(status) {
  if (["HIRED", "OPEN", "Open", "INTERVIEW"].includes(status)) return "ok";
  if (["REJECTED", "DELETED", "CLOSED", "Closed"].includes(status)) return "danger";
  return "warn";
}

async function handleAuthSubmit(event) {
  event.preventDefault();
  const form = event.target;
  const isLogin = state.mode === "login";
  
  const email = form.querySelector('input[type="email"]').value;
  const password = form.querySelector('input[type="password"]').value;
  const nameInput = form.querySelector('input[placeholder="Full Name"]');
  const name = nameInput ? nameInput.value : "";
  const roleSelect = form.querySelector('select');
  const role = roleSelect ? roleSelect.value : state.role;
  
  try {
    const endpoint = isLogin ? "/api/auth/login" : "/api/auth/register";
    const body = isLogin ? { email, password } : { name, email, password, role };
    
    const data = await apiRequest(endpoint, {
      method: "POST",
      body: JSON.stringify(body)
    });
    
    localStorage.setItem("hirehub_access_token", data.accessToken);
    localStorage.setItem("hirehub_refresh_token", data.refreshToken);
    localStorage.setItem("hirehub_user_id", data.id);
    localStorage.setItem("hirehub_user_name", data.name);
    localStorage.setItem("hirehub_user_email", data.email);
    localStorage.setItem("hirehub_user_role", data.role);
    
    setState({
      authed: true,
      role: data.role,
      userName: data.name,
      userEmail: data.email,
      view: "dashboard"
    });
    notify(isLogin ? "Welcome back!" : "Account created successfully.");
  } catch (err) {
    notify(err.message || "Authentication failed.");
  }
}

async function handleLogout() {
  const refreshToken = localStorage.getItem("hirehub_refresh_token");
  if (refreshToken) {
    try {
      await fetch(`${API_BASE}/api/auth/logout`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken })
      });
    } catch (e) {
      console.error("Logout request failed", e);
    }
  }
  localStorage.removeItem("hirehub_access_token");
  localStorage.removeItem("hirehub_refresh_token");
  localStorage.removeItem("hirehub_user_id");
  localStorage.removeItem("hirehub_user_name");
  localStorage.removeItem("hirehub_user_email");
  localStorage.removeItem("hirehub_user_role");
  
  setState({
    authed: false,
    role: "ROLE_APPLICANT",
    view: "dashboard",
    profile: null,
    company: null,
    jobs: [],
    applications: [],
    savedJobs: new Set(),
    users: [],
    companies: []
  });
  notify("Logged out successfully.");
}

async function handleProfileSubmit(event) {
  event.preventDefault();
  const phone = document.getElementById("profile-phone").value;
  const location = document.getElementById("profile-location").value;
  const summary = document.getElementById("profile-summary").value;
  const skillsStr = document.getElementById("profile-skills").value;
  const institution = document.getElementById("profile-institution").value;
  const degree = document.getElementById("profile-degree").value;
  
  const skills = skillsStr.split(",").map(s => s.trim()).filter(s => s.length > 0);
  const education = [{ institution, degree, startYear: 2022, endYear: 2026 }];
  const experience = [];
  
  try {
    const updatedProfile = await apiRequest("/api/applicant-profile/me", {
      method: "PUT",
      body: JSON.stringify({ phone, location, summary, education, experience, skills })
    });
    state.profile = updatedProfile;
    state.applicantProfileComplete = updatedProfile.complete;
    notify("Profile updated successfully!");
    render();
  } catch (err) {
    notify(err.message || "Failed to update profile");
  }
}

async function handleResumeSubmit(event) {
  event.preventDefault();
  const fileInput = document.getElementById("resume-file");
  if (!fileInput.files || fileInput.files.length === 0) {
    notify("Please select a file first");
    return;
  }
  const file = fileInput.files[0];
  const formData = new FormData();
  formData.append("file", file);

  try {
    const resume = await apiRequest("/api/applicant-profile/me/resume", {
      method: "POST",
      body: formData
    });
    notify("Resume uploaded successfully!");
    if (state.profile) {
      state.profile.resume = resume;
    }
    render();
  } catch (err) {
    notify(err.message || "Failed to upload resume");
  }
}

async function handleCompanyCreate(event) {
  event.preventDefault();
  const name = document.getElementById("company-name").value;
  const description = document.getElementById("company-description").value;
  const location = document.getElementById("company-location").value;
  const website = document.getElementById("company-website").value;

  try {
    const company = await apiRequest("/api/companies", {
      method: "POST",
      body: JSON.stringify({ name, description, location, website })
    });
    state.company = company;
    notify("Company registered successfully!");
    setState({ view: "company" });
  } catch (err) {
    notify(err.message || "Failed to register company");
  }
}

async function handleCompanyUpdate(event) {
  event.preventDefault();
  const name = document.getElementById("company-name").value;
  const description = document.getElementById("company-description").value;
  const location = document.getElementById("company-location").value;
  const website = document.getElementById("company-website").value;

  try {
    const company = await apiRequest("/api/companies/me", {
      method: "PUT",
      body: JSON.stringify({ name, description, location, website })
    });
    state.company = company;
    notify("Company updated successfully!");
    setState({ view: "company" });
  } catch (err) {
    notify(err.message || "Failed to update company");
  }
}

async function handleJobCreateSubmit(event) {
  event.preventDefault();
  const title = document.getElementById("job-title").value;
  const description = document.getElementById("job-description").value;
  const location = document.getElementById("job-location").value;
  const employmentType = document.getElementById("job-employment").value;
  const experienceLevel = document.getElementById("job-experience").value;
  const salary = parseFloat(document.getElementById("job-salary").value);
  const deadline = document.getElementById("job-deadline").value;

  try {
    await apiRequest("/api/jobs", {
      method: "POST",
      body: JSON.stringify({ title, description, location, employmentType, experienceLevel, salary, deadline })
    });
    notify("Job posted successfully!");
    setState({ view: "jobs" });
  } catch (err) {
    notify(err.message || "Failed to post job");
  }
}

async function handleJobEditSubmit(event, jobId) {
  event.preventDefault();
  const title = document.getElementById("job-title").value;
  const description = document.getElementById("job-description").value;
  const location = document.getElementById("job-location").value;
  const employmentType = document.getElementById("job-employment").value;
  const experienceLevel = document.getElementById("job-experience").value;
  const salary = parseFloat(document.getElementById("job-salary").value);
  const deadline = document.getElementById("job-deadline").value;

  try {
    await apiRequest(`/api/jobs/${jobId}`, {
      method: "PUT",
      body: JSON.stringify({ title, description, location, employmentType, experienceLevel, salary, deadline })
    });
    notify("Job updated successfully!");
    setState({ view: "jobs" });
  } catch (err) {
    notify(err.message || "Failed to update job");
  }
}

async function applyJob(id) {
  if (!state.applicantProfileComplete) {
    notify("Complete your applicant profile before applying to jobs.");
    setState({ view: "profile" });
    return;
  }
  if (state.applications.some(app => app.jobId === id)) {
    notify("Applicants can apply only once to a job.");
    return;
  }
  try {
    const app = await apiRequest(`/api/applications/jobs/${id}`, {
      method: "POST"
    });
    state.applications.push(app);
    notify("Application submitted successfully.");
    render();
  } catch (err) {
    notify(err.message || "Failed to submit application");
  }
}

async function toggleSave(id) {
  const isSaved = state.savedJobs.has(id);
  try {
    if (isSaved) {
      await apiRequest(`/api/saved-jobs/${id}`, {
        method: "DELETE"
      });
      state.savedJobs.delete(id);
      notify("Job removed from saved jobs.");
    } else {
      await apiRequest(`/api/saved-jobs/${id}`, {
        method: "POST"
      });
      state.savedJobs.add(id);
      notify("Job saved.");
    }
    render();
  } catch (err) {
    notify(err.message || "Failed to toggle saved job");
  }
}

async function updateApplicationStatus(applicationId, newStatus) {
  try {
    await apiRequest(`/api/applications/${applicationId}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status: newStatus })
    });
    notify("Application status updated.");
    loadData();
  } catch (err) {
    notify(err.message || "Failed to update status");
  }
}

async function toggleUser(id) {
  const user = state.users.find(item => item.id === id);
  if (!user) return;
  const endpoint = user.enabled ? `/api/users/${id}/disable` : `/api/users/${id}/enable`;
  try {
    const updated = await apiRequest(endpoint, { method: "PATCH" });
    user.enabled = updated.enabled;
    notify(`User ${user.enabled ? "enabled" : "disabled"}.`);
    render();
  } catch (err) {
    notify(err.message || "Failed to update user status");
  }
}

async function deleteUser(id) {
  if (!confirm("Are you sure you want to delete this user?")) return;
  try {
    await apiRequest(`/api/users/${id}`, { method: "DELETE" });
    state.users = state.users.filter(u => u.id !== id);
    notify("User deleted successfully.");
    render();
  } catch (err) {
    notify(err.message || "Failed to delete user");
  }
}

async function deleteCompany(id) {
  if (!confirm("Are you sure you want to delete this company?")) return;
  try {
    await apiRequest(`/api/companies/${id}`, { method: "DELETE" });
    state.companies = state.companies.filter(c => c.id !== id);
    notify("Company deleted successfully.");
    render();
  } catch (err) {
    notify(err.message || "Failed to delete company");
  }
}

async function deleteJobOwned(id) {
  if (!confirm("Are you sure you want to delete this job posting?")) return;
  try {
    await apiRequest(`/api/jobs/${id}`, { method: "DELETE" });
    state.jobs = state.jobs.filter(j => j.id !== id);
    notify("Job posting deleted.");
    render();
  } catch (err) {
    notify(err.message || "Failed to delete job");
  }
}

async function deleteJobByAdmin(id) {
  if (!confirm("Are you sure you want to delete this job?")) return;
  try {
    await apiRequest(`/api/jobs/${id}/admin`, { method: "DELETE" });
    state.jobs = state.jobs.filter(j => j.id !== id);
    notify("Job deleted by admin.");
    render();
  } catch (err) {
    notify(err.message || "Failed to delete job");
  }
}

function editJob(id) {
  const job = state.jobs.find(j => j.id === id);
  if (job) {
    state.editingJob = job;
    setState({ view: "edit-job" });
  }
}

function authScreen() {
  const isLogin = state.mode === "login";
  return `
    <main class="auth-layout">
      <section class="auth-hero">
        <div class="brand">
          <div class="brand-mark">HH</div>
          <div><h1>HireHub</h1><span>Job portal platform</span></div>
        </div>
        <div>
          <h1>HireHub</h1>
          <p>A role-aware job portal for applicants, recruiters, and platform administrators with clean REST concepts, validation-first flows, and production-minded data rules.</p>
        </div>
      </section>
      <section class="auth-card">
        <div>
          <h2>${isLogin ? "Welcome back" : "Create your account"}</h2>
          <p class="description">${isLogin ? "Sign in to manage postings or discover jobs." : "Registration enforces unique email, required name, and password length rules in the product design."}</p>
        </div>
        <div class="tabs">
          <button class="${isLogin ? "active" : ""}" onclick="setState({mode:'login'})">Login</button>
          <button class="${!isLogin ? "active" : ""}" onclick="setState({mode:'register'})">Register</button>
        </div>
        <form class="form" onsubmit="handleAuthSubmit(event)">
          ${isLogin ? "" : `<div class="field"><label>Name</label><input required placeholder="Full Name" minlength="2" value="" /></div>`}
          <div class="field"><label>Email</label><input required type="email" placeholder="email@example.com" value="" /></div>
          <div class="field"><label>Password</label><input required type="password" placeholder="••••••••" minlength="8" value="" /></div>
          ${isLogin ? "" : `
            <div class="field">
              <label>Register As</label>
              <select name="role">
                <option value="ROLE_APPLICANT">Applicant</option>
                <option value="ROLE_RECRUITER">Recruiter</option>
                <option value="ROLE_ADMIN">Admin</option>
              </select>
            </div>
          `}
          <button class="btn primary" type="submit">${isLogin ? "Login" : "Register"}</button>
        </form>
      </section>
    </main>
    ${toast()}
  `;
}

function shell() {
  const nav = navByRole[state.role];
  if (!nav.some(([view]) => view === state.view) && !["create-job", "edit-job"].includes(state.view)) {
    state.view = "dashboard";
  }
  return `
    <div class="shell">
      <aside class="sidebar">
        <div class="brand">
          <div class="brand-mark">HH</div>
          <div><h1>HireHub</h1><span>${roleNames[state.role]} workspace</span></div>
        </div>
        <nav class="nav">
          ${nav.map(([view, label]) => `<button class="${state.view === view ? "active" : ""}" onclick="setState({view:'${view}'})"><span>${icons[view] || "•"}</span>${label}</button>`).join("")}
        </nav>
        <div class="sidebar-note">Layered architecture target: Controllers handle requests, services own business rules, repositories persist data, and DTOs protect entities.</div>
      </aside>
      <main class="main">
        ${topbar()}
        <section class="content">${page()}</section>
      </main>
    </div>
    ${toast()}
  `;
}

function topbar() {
  const title = `${roleNames[state.role]} ${state.view[0].toUpperCase()}${state.view.slice(1).replace("-", " ")}`;
  return `
    <header class="topbar">
      <div>
        <h2>${title}</h2>
        <p>JWT protected workspace with validation-aware workflows and role-based access.</p>
      </div>
      <div class="actions">
        ${state.role === "ROLE_APPLICANT" ? `<button class="btn ghost" onclick="setState({view:'profile'})">Update profile</button>` : ""}
        <button class="btn" onclick="handleLogout()">Logout</button>
      </div>
    </header>
  `;
}

function metrics() {
  const recruiterJobs = state.jobs.length;
  const rows = state.role === "ROLE_APPLICANT"
    ? [
        ["Open jobs", state.jobs.filter(j => j.status === "OPEN" || j.status === "Open").length], 
        ["Saved jobs", state.savedJobs.size], 
        ["Applications", state.applications.length], 
        ["Interviews", state.applications.filter(a => a.status === "INTERVIEW").length]
      ]
    : state.role === "ROLE_RECRUITER"
      ? [
          ["My jobs", recruiterJobs], 
          ["Applicants", state.applications.length], 
          ["Open roles", state.jobs.filter(j => j.status === "OPEN" || j.status === "Open").length], 
          ["Company", state.company ? 1 : 0]
        ]
      : state.adminStats
        ? [
            ["Users", state.adminStats.users], 
            ["Companies", state.adminStats.companies], 
            ["Jobs", state.adminStats.jobs], 
            ["Applications", state.adminStats.applications]
          ]
        : [
            ["Users", state.users.length], 
            ["Companies", state.companies.length], 
            ["Jobs", state.jobs.length], 
            ["Disabled", state.users.filter(u => !u.enabled).length]
          ];
  return `<div class="metrics">${rows.map(([label, value]) => `<div class="metric"><span>${label}</span><strong>${value}</strong></div>`).join("")}</div>`;
}

function page() {
  if (state.view === "dashboard") return dashboard();
  if (state.view === "jobs") return jobsPage();
  if (state.view === "applications") return applicationsPage();
  if (state.view === "profile") return profilePage();
  if (state.view === "applicants") return applicantsPage();
  if (state.view === "company") return companyPage();
  if (state.view === "users") return usersPage();
  if (state.view === "companies") return companiesPage();
  if (state.view === "create-job") return createJobPage();
  if (state.view === "edit-job") return editJobPage();
  return dashboard();
}

function dashboard() {
  return `
    ${metrics()}
    <div class="grid">
      <section class="panel">
        <div class="panel-header"><div><h3>Priority work</h3><p>Development order mapped into product modules.</p></div></div>
        <div class="stack">
          ${["Authentication and JWT", "User management", "Company and job modules", "Applicant profile and resume upload", "Applications, saved jobs, search and filters", "Admin dashboard, Docker and deployment"].map((item, index) => `
            <div class="card job-card">
              <div><h4>${index + 1}. ${item}</h4><p class="description">Keep controllers thin, put business rules in services, validate DTOs, and return consistent API responses.</p></div>
              <span class="pill ok">Live</span>
            </div>
          `).join("")}
        </div>
      </section>
      <section class="panel">
        <div class="panel-header"><div><h3>Security contract</h3><p>Rules reflected in the interface.</p></div></div>
        <div class="stack">
          ${["BCrypt passwords", "Stateless JWT auth", "Role-based authorization", "PDF resume under 5MB", "No duplicate applications", "Admins only delete users"].map(rule => `<div class="card"><strong>${rule}</strong><p class="description">Designed as a protected API workflow with validation feedback.</p></div>`).join("")}
        </div>
      </section>
    </div>
  `;
}

function filters() {
  const locations = ["All", ...new Set(state.jobs.map(job => job.location))];
  const employment = ["All", "Full-time", "Part-time", "Contract", "Internship"];
  const sorts = ["Newest", "Oldest", "Highest Salary", "Lowest Salary", "Alphabetical"];
  return `
    <div class="filters">
      <input class="search" placeholder="Search title, keyword" value="${state.query}" oninput="state.query=this.value; searchJobs();" />
      <select onchange="state.location=this.value; searchJobs();">${locations.map(v => `<option ${state.location === v ? "selected" : ""}>${v}</option>`).join("")}</select>
      <select onchange="state.employment=this.value; searchJobs();">${employment.map(v => `<option ${state.employment === v ? "selected" : ""}>${v}</option>`).join("")}</select>
      <select onchange="setState({sort:this.value})">${sorts.map(v => `<option ${state.sort === v ? "selected" : ""}>${v}</option>`).join("")}</select>
      <button class="btn" onclick="setState({query:'', location:'All', employment:'All', sort:'Newest'}); searchJobs();">Reset</button>
    </div>
  `;
}

function jobsPage() {
  const canCreate = state.role === "ROLE_RECRUITER";
  return `
    <section class="panel">
      <div class="panel-header">
        <div><h3>${state.role === "ROLE_RECRUITER" ? "My job postings" : "Job discovery"}</h3><p>Search, filter, sort, paginate-ready listings.</p></div>
        ${canCreate ? `<button class="btn primary" onclick="if (!state.company) { notify('Please register your company first.'); setState({view:'company'}); } else { setState({view:'create-job'}); }">Create job</button>` : ""}
      </div>
      ${state.role !== "ROLE_RECRUITER" ? filters() : ""}
      <div class="job-list">
        ${filteredJobs().map(jobCard).join("") || `<div class="empty">No jobs match the current filters.</div>`}
      </div>
    </section>
  `;
}

function jobCard(job) {
  const alreadyApplied = state.applications.some(app => app.jobId === job.id);
  const saved = state.savedJobs.has(job.id);
  const applyLabel = alreadyApplied ? "Applied" : job.status === "CLOSED" || job.status === "Closed" ? "Closed" : state.applicantProfileComplete ? "Apply" : "Complete profile";
  return `
    <article class="card job-card">
      <div>
        <h4>${job.title}</h4>
        <div class="meta"><span>${job.companyName || "HireHub Company"}</span><span>${job.location}</span><span>${job.employmentType || "Full-time"}</span><span>${money(job.salary)}</span></div>
        <p class="description">${job.description}</p>
        <div class="meta"><span class="pill ${statusClass(job.status)}">${job.status}</span><span class="pill">Deadline ${job.deadline}</span></div>
      </div>
      <div class="actions">
        ${state.role === "ROLE_APPLICANT" ? `<button class="btn" onclick="toggleSave(${job.id})">${saved ? "Saved" : "Save"}</button><button class="btn primary" ${alreadyApplied || job.status === "CLOSED" || job.status === "Closed" ? "disabled" : ""} onclick="applyJob(${job.id})">${applyLabel}</button>` : ""}
        ${state.role === "ROLE_RECRUITER" ? `<button class="btn" onclick="editJob(${job.id})">Update</button><button class="btn danger" onclick="deleteJobOwned(${job.id})">Delete</button>` : ""}
        ${state.role === "ROLE_ADMIN" ? `<button class="btn danger" onclick="deleteJobByAdmin(${job.id})">Delete</button>` : ""}
      </div>
    </article>
  `;
}

function applicationsPage() {
  return `
    <section class="panel">
      <div class="panel-header"><div><h3>Application tracker</h3><p>Track APPLIED, UNDER_REVIEW, INTERVIEW, REJECTED, and HIRED states.</p></div></div>
      <table class="table">
        <thead><tr><th>Job</th><th>Company</th><th>Status</th></tr></thead>
        <tbody>
          ${state.applications.map(app => `
            <tr>
              <td>${app.jobTitle || "Job Opportunity"}</td>
              <td>${app.companyName || "Company"}</td>
              <td><span class="pill ${statusClass(app.status)}">${app.status}</span></td>
            </tr>
          `).join("") || `<tr><td colspan="3" class="empty">You have not submitted any applications.</td></tr>`}
        </tbody>
      </table>
    </section>
  `;
}

function profilePage() {
  const p = state.profile || { phone: "", location: "", summary: "", education: [], skills: [] };
  const edu = p.education && p.education[0] ? p.education[0] : { institution: "", degree: "" };
  return `
    <div class="grid">
      <section class="panel">
        <div class="panel-header"><div><h3>Applicant profile</h3><p>Applicants can edit only their own profile.</p></div><span class="pill ${state.applicantProfileComplete ? "ok" : "warn"}">${state.applicantProfileComplete ? "Complete" : "Required"}</span></div>
        <form class="form" onsubmit="handleProfileSubmit(event)">
          <div class="split">
            <div class="field"><label>Phone</label><input id="profile-phone" value="${p.phone || ""}" required placeholder="+91 XXXXXXXXXX" /></div>
            <div class="field"><label>Location</label><input id="profile-location" value="${p.location || ""}" required placeholder="e.g. Pune, Remote" /></div>
          </div>
          <div class="field"><label>Summary</label><textarea id="profile-summary" required placeholder="Tell recruiters about yourself...">${p.summary || ""}</textarea></div>
          <div class="field"><label>Skills</label><input id="profile-skills" value="${p.skills ? p.skills.join(", ") : ""}" placeholder="Java, React, PostgreSQL" required /></div>
          <div class="split">
            <div class="field"><label>Institution</label><input id="profile-institution" value="${edu.institution || ""}" required placeholder="e.g. Pune University" /></div>
            <div class="field"><label>Degree</label><input id="profile-degree" value="${edu.degree || ""}" required placeholder="e.g. B.Tech Computer Science" /></div>
          </div>
          <button class="btn primary">Save profile</button>
        </form>
      </section>
      <section class="panel">
        <div class="panel-header"><div><h3>Resume</h3><p>PDF only, maximum 5MB.</p></div></div>
        <form class="form" onsubmit="handleResumeSubmit(event)">
          <div class="field">
            <label>Upload resume</label>
            <input type="file" id="resume-file" accept="application/pdf" required />
            ${p.resume ? `<div class="meta" style="margin-top:10px;">Current: <a href="${p.resume.filePath}" target="_blank">${p.resume.originalFileName}</a> (${(p.resume.sizeBytes/1024/1024).toFixed(2)} MB)</div>` : ""}
          </div>
          <button class="btn primary">Upload resume</button>
        </form>
      </section>
    </div>
  `;
}

function applicantsPage() {
  return `
    <section class="panel">
      <div class="panel-header"><div><h3>Applicants</h3><p>Recruiters can view applicants for their own jobs and update status.</p></div></div>
      <table class="table"><thead><tr><th>Applicant</th><th>Job</th><th>Status</th><th>Action</th></tr></thead><tbody>
        ${state.applications.map(app => `
          <tr>
            <td>${app.applicantName}</td>
            <td>${app.jobTitle}</td>
            <td><span class="pill ${statusClass(app.status)}">${app.status}</span></td>
            <td>
              <select onchange="updateApplicationStatus(${app.id}, this.value)">
                <option value="APPLIED" ${app.status === "APPLIED" ? "selected" : ""}>APPLIED</option>
                <option value="UNDER_REVIEW" ${app.status === "UNDER_REVIEW" ? "selected" : ""}>UNDER_REVIEW</option>
                <option value="INTERVIEW" ${app.status === "INTERVIEW" ? "selected" : ""}>INTERVIEW</option>
                <option value="REJECTED" ${app.status === "REJECTED" ? "selected" : ""}>REJECTED</option>
                <option value="HIRED" ${app.status === "HIRED" ? "selected" : ""}>HIRED</option>
              </select>
            </td>
          </tr>
        `).join("") || `<tr><td colspan="4" class="empty">No applications received yet.</td></tr>`}
      </tbody></table>
    </section>
  `;
}

function companyPage() {
  const c = state.company;
  if (!c) {
    return `
      <section class="panel">
        <div class="panel-header"><div><h3>Register Company</h3><p>Recruiters belong to exactly one company. Create your company profile first.</p></div></div>
        <form class="form" onsubmit="handleCompanyCreate(event)">
          <div class="field"><label>Company Name</label><input id="company-name" required placeholder="e.g. Northstar Labs" /></div>
          <div class="field"><label>Description</label><textarea id="company-description" required placeholder="Describe your company..."></textarea></div>
          <div class="field"><label>Location</label><input id="company-location" required placeholder="e.g. Bengaluru, Remote" /></div>
          <div class="field"><label>Website</label><input id="company-website" placeholder="e.g. https://northstar.dev" /></div>
          <button class="btn primary">Create Company</button>
        </form>
      </section>
    `;
  }
  return `
    <div class="grid">
      <section class="panel">
        <div class="panel-header"><div><h3>Company profile</h3><p>Recruiters belong to exactly one company in this version.</p></div></div>
        <form class="form" onsubmit="handleCompanyUpdate(event)">
          <div class="field"><label>Company name</label><input id="company-name" required value="${c.name}" /></div>
          <div class="field"><label>Description</label><textarea id="company-description">${c.description || ""}</textarea></div>
          <div class="field"><label>Location</label><input id="company-location" value="${c.location || ""}" /></div>
          <div class="field"><label>Website</label><input id="company-website" value="${c.website || ""}" /></div>
          <button class="btn primary">Update company</button>
        </form>
      </section>
      <section class="panel">
        <div class="panel-header"><div><h3>Ownership rules</h3><p>Recruiters cannot modify another recruiter’s company.</p></div></div>
        <div class="stack">
          <div class="card"><strong>Owner</strong><p class="description">${c.ownerName || "You"}</p></div>
          <div class="card"><strong>Active postings</strong><p class="description">${state.jobs.length} owned jobs</p></div>
        </div>
      </section>
    </div>
  `;
}

function createJobPage() {
  return `
    <section class="panel">
      <div class="panel-header"><div><h3>Create Job Posting</h3><p>Specify description, location, type, and salary details.</p></div></div>
      <form class="form" onsubmit="handleJobCreateSubmit(event)">
        <div class="field"><label>Job Title</label><input id="job-title" required placeholder="e.g. Backend Developer Intern" /></div>
        <div class="field"><label>Description</label><textarea id="job-description" required placeholder="Build REST APIs..."></textarea></div>
        <div class="field"><label>Location</label><input id="job-location" required placeholder="e.g. Bengaluru, Remote" /></div>
        <div class="split">
          <div class="field">
            <label>Employment Type</label>
            <select id="job-employment">
              <option value="FULL_TIME">Full-time</option>
              <option value="PART_TIME">Part-time</option>
              <option value="CONTRACT">Contract</option>
              <option value="INTERNSHIP">Internship</option>
            </select>
          </div>
          <div class="field">
            <label>Experience Level</label>
            <select id="job-experience">
              <option value="FRESHER">Fresher</option>
              <option value="JUNIOR">Junior (1-2 years)</option>
              <option value="MID_LEVEL">Mid-level (3-5 years)</option>
              <option value="SENIOR">Senior (5+ years)</option>
            </select>
          </div>
        </div>
        <div class="split">
          <div class="field"><label>Monthly Salary (INR)</label><input type="number" id="job-salary" required min="0" placeholder="e.g. 50000" /></div>
          <div class="field"><label>Deadline</label><input type="date" id="job-deadline" required /></div>
        </div>
        <div class="actions">
          <button class="btn primary" type="submit">Post Job</button>
          <button class="btn" type="button" onclick="setState({view:'jobs'})">Cancel</button>
        </div>
      </form>
    </section>
  `;
}

function editJobPage() {
  const job = state.editingJob;
  if (!job) {
    setState({view: 'jobs'});
    return "";
  }
  return `
    <section class="panel">
      <div class="panel-header"><div><h3>Edit Job Posting</h3><p>Modify description, location, type, and salary details.</p></div></div>
      <form class="form" onsubmit="handleJobEditSubmit(event, ${job.id})">
        <div class="field"><label>Job Title</label><input id="job-title" required value="${job.title}" /></div>
        <div class="field"><label>Description</label><textarea id="job-description" required>${job.description}</textarea></div>
        <div class="field"><label>Location</label><input id="job-location" required value="${job.location}" /></div>
        <div class="split">
          <div class="field">
            <label>Employment Type</label>
            <select id="job-employment">
              <option value="FULL_TIME" ${job.employmentType === "FULL_TIME" ? "selected" : ""}>Full-time</option>
              <option value="PART_TIME" ${job.employmentType === "PART_TIME" ? "selected" : ""}>Part-time</option>
              <option value="CONTRACT" ${job.employmentType === "CONTRACT" ? "selected" : ""}>Contract</option>
              <option value="INTERNSHIP" ${job.employmentType === "INTERNSHIP" ? "selected" : ""}>Internship</option>
            </select>
          </div>
          <div class="field">
            <label>Experience Level</label>
            <select id="job-experience">
              <option value="FRESHER" ${job.experienceLevel === "FRESHER" ? "selected" : ""}>Fresher</option>
              <option value="JUNIOR" ${job.experienceLevel === "JUNIOR" ? "selected" : ""}>Junior (1-2 years)</option>
              <option value="MID_LEVEL" ${job.experienceLevel === "MID_LEVEL" ? "selected" : ""}>Mid-level (3-5 years)</option>
              <option value="SENIOR" ${job.experienceLevel === "SENIOR" ? "selected" : ""}>Senior (5+ years)</option>
            </select>
          </div>
        </div>
        <div class="split">
          <div class="field"><label>Monthly Salary (INR)</label><input type="number" id="job-salary" required min="0" value="${job.salary}" /></div>
          <div class="field"><label>Deadline</label><input type="date" id="job-deadline" required value="${job.deadline}" /></div>
        </div>
        <div class="actions">
          <button class="btn primary" type="submit">Save Changes</button>
          <button class="btn" type="button" onclick="setState({view:'jobs'})">Cancel</button>
        </div>
      </form>
    </section>
  `;
}

function usersPage() {
  return `
    <section class="panel">
      <div class="panel-header"><div><h3>User management</h3><p>Admin-only user disable and delete operations.</p></div></div>
      <table class="table"><thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Action</th></tr></thead><tbody>
        ${state.users.map(user => `
          <tr>
            <td>${user.name}</td>
            <td>${user.email}</td>
            <td>${roleNames[user.role] || user.role}</td>
            <td><span class="pill ${user.enabled ? "ok" : "danger"}">${user.enabled ? "Enabled" : "Disabled"}</span></td>
            <td>
              <button class="btn" onclick="toggleUser(${user.id})">${user.enabled ? "Disable" : "Enable"}</button> 
              <button class="btn danger" onclick="deleteUser(${user.id})">Delete</button>
            </td>
          </tr>
        `).join("") || `<tr><td colspan="5" class="empty">No users found.</td></tr>`}
      </tbody></table>
    </section>
  `;
}

function companiesPage() {
  return `
    <section class="panel">
      <div class="panel-header"><div><h3>Company management</h3><p>Admin dashboard for companies and recruiter ownership.</p></div></div>
      <table class="table"><thead><tr><th>Company</th><th>Owner ID</th><th>Action</th></tr></thead><tbody>
        ${state.companies.map(company => `
          <tr>
            <td>${company.name}</td>
            <td>User #${company.ownerUserId || "Unknown"}</td>
            <td><button class="btn danger" onclick="deleteCompany(${company.id})">Delete</button></td>
          </tr>
        `).join("") || `<tr><td colspan="3" class="empty">No companies found.</td></tr>`}
      </tbody></table>
    </section>
  `;
}

function toast() {
  return state.toast ? `<div class="toast">${state.toast}</div>` : "";
}

function render() {
  document.getElementById("app").innerHTML = state.authed ? shell() : authScreen();
}

// Initial boot
loadData();
render();
