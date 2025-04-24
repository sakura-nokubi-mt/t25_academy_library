package jp.co.metateam.library.controller;

import java.lang.reflect.Member;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.BookMstService;
import lombok.extern.log4j.Log4j2;


/**
 * 書籍関連クラス
 */
@Log4j2
@Controller

public class BookController {
    
    private final BookMstService bookMstService;

    @Autowired
    public BookController(BookMstService bookMstService){
        this.bookMstService = bookMstService;
    }

    @GetMapping("/book/index")
    public String index(Model model) {
        // 書籍を全件取得
        List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();
        
        model.addAttribute("bookMstList", bookMstList);

        return "book/index";
    }

    @GetMapping("/book/add")
    public String add(Model model) {
        if (!model.containsAttribute("bookMstDto")) {
            model.addAttribute("bookMstDto", new BookMstDto());
        }

        return "book/add";
    }

    /**
     * @param <bookMstDto>
     * @param bookMstDto
     * @param result
     * @param ra
     * @return
     */
    @PostMapping("/book/add")
    public <bookMstDto> String add(@Valid @ModelAttribute BookMstDto bookMstDto, BindingResult result, RedirectAttributes ra) {
        try {
            
            boolean errIsbnFlg = false;
            boolean errTitleFlg = false; 
            String title = bookMstDto.getTitle();
            String isbn = bookMstDto.getIsbn();

            //書籍必須
            if (StringUtils.isEmpty(title)){
                result.rejectValue("title", "error.value", "書籍名は必須です");
                errTitleFlg = true;
            }
            //書籍桁数
            if (title.length() >= 256){
                result.rejectValue("title", "error.value", "書籍名は255文字以下で入力してください");
                errTitleFlg = true;
            }
            //ISBN必須
            if (StringUtils.isEmpty(title)){
                result.rejectValue("isbn", "error.value", "ISBNは必須です");
                errIsbnFlg = true;
            }
            //ISBN桁数
            if (isbn.length() != 13){
                result.rejectValue("isbn", "error.value", "ISBNは13桁で入力してください");
                errIsbnFlg = true;
            }
            //ISBN文字種
            if (!isbn.matches("^[0-9]*$")){
                result.rejectValue("isbn", "error.value", "ISBNは半角数字で入力してください");
                errIsbnFlg = true;
            }
           //重複チェック
           int isbnExist = this.bookMstService.selectByIsbn(isbn);//データ取得
            if(isbnExist >= 1){
                result.rejectValue("isbn", "error.value", "登録済みのISBNです");
                errIsbnFlg = true;
            }

            if (errIsbnFlg || errTitleFlg) {
                throw new Exception();
            }
            
            bookMstService.save(bookMstDto); 
            return "redirect:/book/index";

        } catch (Exception e) {
        log.error(e.getMessage());

        ra.addFlashAttribute("bookMstDto", bookMstDto);
        ra.addFlashAttribute("org.springframework.validation.BindingResult.bookMstDto", result);

            return "book/add"; //redirectいらない？
        }
    }
}
