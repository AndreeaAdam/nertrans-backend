package ro.nertrans.filters;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.nertrans.filters.filteredDTOS.PartnerSearchDTO;
import ro.nertrans.filters.filteredDTOS.UserSearchDTO;
import ro.nertrans.filters.filteredServices.PartnerSearchService;
import ro.nertrans.filters.filteredServices.UserSearchService;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
public class SearchController {
    @Autowired
    private UserSearchService userSearchService;
    @Autowired
    private PartnerSearchService partnerSearchService;

    /**
     * @param user UserFilterDTO
     * @param page - current Page ( starts with 0 )
     * @param size -
     * @param sort - what to sort by . ex : email - sorts by email
     * @param dir  - asc / desc
     * @return Page<User>
     * @Description: Filtered and paginated list of users
     */
    @RequestMapping(value = "/listUsersFiltered", method = RequestMethod.POST)
    @ApiResponse(description = "Filtered and paginated list of users")
    public ResponseEntity<?> listUsersFiltered(@RequestBody UserSearchDTO user,
                                               @RequestParam(value = "page") int page,
                                               @RequestParam(value = "size") int size,
                                               @RequestParam(value = "sort", required = false) String sort,
                                               @RequestParam(value = "dir", required = false) String dir,
                                               HttpServletRequest request) {
        return ResponseEntity.ok(userSearchService.listUsersFiltered(user, page, size, sort, dir, request));
    }

    @RequestMapping(value = "/listPartnerFiltered", method = RequestMethod.POST)
    @ApiResponse(description = "Filtered and paginated list of partners")
    public ResponseEntity<?> listPartnerFiltered(@RequestBody PartnerSearchDTO partnerSearchDTO,
                                               @RequestParam(value = "page") int page,
                                               @RequestParam(value = "size") int size,
                                               @RequestParam(value = "sort", required = false) String sort,
                                               @RequestParam(value = "dir", required = false) String dir,
                                               HttpServletRequest request) {
        return ResponseEntity.ok(partnerSearchService.listPartnerFiltered(partnerSearchDTO, page, size, sort, dir, request));
    }

}
