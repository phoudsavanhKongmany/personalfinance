package com.finance.personalfinance;

import dao.AccountDAO;
import dao.CategoryDAO;
import dao.DebtDAO;
import dao.InvestmentDAO;
import dao.TransactionDAO;
import dao.UserDAO;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import model.Account;
import model.Category;
import model.Debt;
import model.Investment;
import model.Transaction;
import model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FinanceController {

	private final UserDAO userDao = new UserDAO();
	private final TransactionDAO transactionDao = new TransactionDAO();
	private final CategoryDAO categoryDao = new CategoryDAO();
	private final AccountDAO accountDao = new AccountDAO();
	private final DebtDAO debtDao = new DebtDAO();
	private final InvestmentDAO investmentDao = new InvestmentDAO();

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@PostMapping("/auth")
	public String auth(@RequestParam String action, @RequestParam(required = false) String username,
			@RequestParam(required = false) String password, @RequestParam(required = false) String fullName,
			HttpSession session, RedirectAttributes redirectAttributes) {
		if ("login".equals(action)) {
			User user = userDao.login(username, password);
			if (user == null) {
				redirectAttributes.addFlashAttribute("error", "Sai ten dang nhap hoac mat khau.");
				return "redirect:/login";
			}
			session.setAttribute("loggedUser", user);
			return "redirect:/dashboard";
		}

		if ("register".equals(action)) {
			User user = new User();
			user.setUsername(username);
			user.setPassword(password);
			user.setFullName(fullName);
			if (userDao.register(user)) {
				redirectAttributes.addFlashAttribute("success", "Dang ky thanh cong. Hay dang nhap.");
				return "redirect:/login";
			}
			redirectAttributes.addFlashAttribute("error", "Ten dang nhap da ton tai.");
			return "redirect:/register";
		}

		if ("logout".equals(action)) {
			session.invalidate();
		}
		return "redirect:/login";
	}

	@GetMapping("/auth")
	public String authGet(@RequestParam(required = false) String action, HttpSession session) {
		if ("logout".equals(action)) {
			session.invalidate();
		}
		return "redirect:/login";
	}

	@GetMapping("/dashboard")
	public String dashboard(@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year,
			HttpSession session, Model model) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";

		int selectedMonth = month != null ? month : LocalDate.now().getMonthValue();
		int selectedYear = year != null ? year : LocalDate.now().getYear();
		int userId = user.getId();

		model.addAttribute("summary", transactionDao.getSummary(userId, selectedMonth, selectedYear));
		model.addAttribute("expenseByCategory", transactionDao.getExpenseByCategory(userId, selectedMonth, selectedYear));
		model.addAttribute("incomeByCategory", transactionDao.getIncomeByCategory(userId, selectedMonth, selectedYear));
		model.addAttribute("recentTransactions", transactionDao.getRecent(userId, 5));
		model.addAttribute("monthly", transactionDao.getLast6Months(userId));
		model.addAttribute("accountStats", accountDao.getAccountStats(userId));
		model.addAttribute("debtStats", debtDao.getDebtStats(userId));
		model.addAttribute("investStats", investmentDao.getInvestStats(userId));
		model.addAttribute("month", selectedMonth);
		model.addAttribute("year", selectedYear);
		return "dashboard";
	}

	@GetMapping("/transactions")
	public String transactions(@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year,
			HttpSession session, Model model) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";

		int selectedMonth = month != null ? month : LocalDate.now().getMonthValue();
		int selectedYear = year != null ? year : LocalDate.now().getYear();
		model.addAttribute("transactions", transactionDao.getByUserAndMonth(user.getId(), selectedMonth, selectedYear));
		model.addAttribute("summary", transactionDao.getSummary(user.getId(), selectedMonth, selectedYear));
		model.addAttribute("month", selectedMonth);
		model.addAttribute("year", selectedYear);
		return "transactions";
	}

	@GetMapping("/transaction")
	public String transactionAction(@RequestParam String action, @RequestParam int id,
			@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year,
			HttpSession session, Model model) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";

		if ("delete".equals(action)) {
			transactionDao.delete(id, user.getId());
			String suffix = month != null && year != null ? "?month=" + month + "&year=" + year : "";
			return "redirect:/transactions" + suffix;
		}

		Transaction transaction = transactionDao.getById(id, user.getId());
		if (transaction == null) return "redirect:/transactions";
		model.addAttribute("transaction", transaction);
		model.addAttribute("categories", categoryDao.getByUserAndType(user.getId(), transaction.getType()));
		return "transaction-form";
	}

	@GetMapping("/transactions/new")
	public String addTransaction(HttpSession session, Model model) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";
		model.addAttribute("categories", categoryDao.getByUserAndType(user.getId(), "expense"));
		return "transaction-form";
	}

	@PostMapping("/transactions")
	public String saveTransaction(@RequestParam String action, @RequestParam int categoryId, @RequestParam BigDecimal amount,
			@RequestParam String type, @RequestParam(required = false) String description,
			@RequestParam String transactionDate, @RequestParam(required = false) Integer id, HttpSession session) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";

		Transaction transaction = new Transaction();
		if (id != null) transaction.setId(id);
		transaction.setUserId(user.getId());
		transaction.setCategoryId(categoryId);
		transaction.setAmount(amount);
		transaction.setType(type);
		transaction.setDescription(description);
		transaction.setTransactionDate(LocalDate.parse(transactionDate));

		if ("update".equals(action)) {
			transactionDao.update(transaction);
		} else {
			transactionDao.add(transaction);
		}
		return "redirect:/transactions";
	}

	@GetMapping("/account")
	public String accounts(@RequestParam(required = false) String action, @RequestParam(required = false) Integer id,
			HttpSession session, Model model) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";
		if ("delete".equals(action) && id != null) {
			accountDao.delete(id, user.getId());
			return "redirect:/account";
		}
		model.addAttribute("accounts", accountDao.getByUser(user.getId()));
		return "accounts";
	}

	@PostMapping("/account")
	public String addAccount(@RequestParam String name, @RequestParam String type, @RequestParam BigDecimal balance,
			@RequestParam(required = false) String description, HttpSession session) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";
		Account account = new Account();
		account.setUserId(user.getId());
		account.setName(name.trim());
		account.setType(type);
		account.setBalance(balance);
		account.setDescription(description);
		accountDao.add(account);
		return "redirect:/account";
	}

	@GetMapping("/category")
	public String categories(@RequestParam(required = false) String action, @RequestParam(required = false) Integer id,
			@RequestParam(defaultValue = "expense") String type, HttpSession session, Model model) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";
		if ("delete".equals(action) && id != null) {
			categoryDao.delete(id, user.getId());
			return "redirect:/category?type=" + type;
		}
		model.addAttribute("categories", categoryDao.getByUserAndType(user.getId(), type));
		model.addAttribute("selectedType", type);
		return "categories";
	}

	@GetMapping("/category/list")
	@ResponseBody
	public List<Category> categoryList(@RequestParam(defaultValue = "expense") String type, HttpSession session) {
		User user = requireUser(session);
		if (user == null) return List.of();
		return categoryDao.getByUserAndType(user.getId(), type);
	}

	@PostMapping("/category")
	public String addCategory(@RequestParam String name, @RequestParam String type, HttpSession session,
			RedirectAttributes redirectAttributes) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";
		Category category = new Category();
		category.setUserId(user.getId());
		category.setName(name.trim());
		category.setType(type);
		boolean added = categoryDao.add(category);
		redirectAttributes.addFlashAttribute(added ? "success" : "error",
				added ? "Them danh muc thanh cong." : "Them danh muc that bai.");
		return "redirect:/category?type=" + type;
	}

	@GetMapping("/debt")
	public String debts(@RequestParam(required = false) String action, @RequestParam(required = false) Integer id,
			HttpSession session, Model model) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";
		if ("delete".equals(action) && id != null) {
			debtDao.delete(id, user.getId());
			return "redirect:/debt";
		}
		if ("done".equals(action) && id != null) {
			debtDao.markDone(id, user.getId());
			return "redirect:/debt";
		}
		model.addAttribute("debts", debtDao.getByUser(user.getId()));
		return "debts";
	}

	@PostMapping("/debt")
	public String addDebt(@RequestParam String name, @RequestParam String type, @RequestParam BigDecimal amount,
			@RequestParam(required = false) String person, @RequestParam(required = false) String dueDate,
			@RequestParam(required = false) String note, HttpSession session) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";
		Debt debt = new Debt();
		debt.setUserId(user.getId());
		debt.setName(name.trim());
		debt.setType(type);
		debt.setAmount(amount);
		debt.setPerson(person);
		debt.setNote(note);
		if (dueDate != null && !dueDate.isBlank()) debt.setDueDate(LocalDate.parse(dueDate));
		debtDao.add(debt);
		return "redirect:/debt";
	}

	@GetMapping("/investment")
	public String investments(@RequestParam(required = false) String action, @RequestParam(required = false) Integer id,
			HttpSession session, Model model) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";
		if ("delete".equals(action) && id != null) {
			investmentDao.delete(id, user.getId());
			return "redirect:/investment";
		}
		model.addAttribute("investments", investmentDao.getByUser(user.getId()));
		return "investments";
	}

	@PostMapping("/investment")
	public String addInvestment(@RequestParam String name, @RequestParam String type,
			@RequestParam BigDecimal investedAmount, @RequestParam BigDecimal currentValue,
			@RequestParam(required = false) String startDate, @RequestParam(required = false) String note,
			HttpSession session) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";
		Investment investment = new Investment();
		investment.setUserId(user.getId());
		investment.setName(name.trim());
		investment.setType(type);
		investment.setInvestedAmount(investedAmount);
		investment.setCurrentValue(currentValue);
		investment.setNote(note);
		if (startDate != null && !startDate.isBlank()) investment.setStartDate(LocalDate.parse(startDate));
		investmentDao.add(investment);
		return "redirect:/investment";
	}

	@GetMapping("/setting")
	public String settings(HttpSession session) {
		return requireUser(session) == null ? "redirect:/login" : "settings";
	}

	@PostMapping("/setting")
	public String updateSettings(@RequestParam String action, @RequestParam(required = false) String fullName,
			@RequestParam(required = false) String oldPassword, @RequestParam(required = false) String newPassword,
			@RequestParam(required = false) String confirmPassword, HttpSession session,
			RedirectAttributes redirectAttributes) {
		User user = requireUser(session);
		if (user == null) return "redirect:/login";

		if ("updateProfile".equals(action)) {
			boolean updated = userDao.updateFullName(user.getId(), fullName.trim());
			if (updated) {
				user.setFullName(fullName.trim());
				session.setAttribute("loggedUser", user);
			}
			redirectAttributes.addFlashAttribute(updated ? "success" : "error",
					updated ? "Cap nhat thanh cong." : "Cap nhat that bai.");
		}

		if ("changePassword".equals(action)) {
			if (!newPassword.equals(confirmPassword)) {
				redirectAttributes.addFlashAttribute("error", "Mat khau xac nhan khong khop.");
			} else if (userDao.checkPassword(user.getId(), oldPassword)) {
				userDao.updatePassword(user.getId(), newPassword);
				redirectAttributes.addFlashAttribute("success", "Doi mat khau thanh cong.");
			} else {
				redirectAttributes.addFlashAttribute("error", "Mat khau cu khong dung.");
			}
		}
		return "redirect:/setting";
	}

	private User requireUser(HttpSession session) {
		return (User) session.getAttribute("loggedUser");
	}
}
