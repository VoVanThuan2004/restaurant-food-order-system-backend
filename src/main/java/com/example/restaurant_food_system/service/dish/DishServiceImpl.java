package com.example.restaurant_food_system.service.dish;

import com.example.restaurant_food_system.dto.request.DishRequest;
import com.example.restaurant_food_system.dto.request.DishVariantGroupRequest;
import com.example.restaurant_food_system.dto.request.DishVariantOptionRequest;
import com.example.restaurant_food_system.dto.response.DishDetailResponse;
import com.example.restaurant_food_system.dto.response.DishResponse;
import com.example.restaurant_food_system.dto.response.DishStatusResponse;
import com.example.restaurant_food_system.dto.response.UploadResult;
import com.example.restaurant_food_system.entity.*;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.exception.ResourceNotFoundException;
import com.example.restaurant_food_system.mapper.DishMapper;
import com.example.restaurant_food_system.repository.*;
import com.example.restaurant_food_system.service.CloudinaryService;
import com.example.restaurant_food_system.service.SocketService;
import com.example.restaurant_food_system.utils.RuleSide;
import com.example.restaurant_food_system.utils.ValidateFile;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.digester.Rule;
import org.hibernate.engine.profile.Association;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {
    private final DishRepository dishRepository;
    private final ValidateFile validateFile;
    private final CloudinaryService cloudinaryService;
    private final CategoryRepository categoryRepository;
    private final DishVariantGroupRepository dishVariantGroupRepository;
    private final DishVariantOptionRepository dishVariantOptionRepository;
    private final DishMapper dishMapper;
    private final SocketService socketService;
    private final AssociationRuleRepository associationRuleRepository;

    @Override
    @Transactional
    public void createNewDish(DishRequest dishRequest, MultipartFile file) {
        // 1. Kiểm tra danh mục món ăn có tồn tại
        Category category = categoryRepository.findById(dishRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng upload ảnh món ăn");
        }

        // 2. Validate file ảnh upload
        validateFile.validateFileImage(file);

        UploadResult uploadResult = null;
        try {
            uploadResult = cloudinaryService.uploadFile(file);

            // 3. Tạo dish
            Dish dish = Dish.builder()
                    .image(uploadResult.getSecureUrl())
                    .publicId(uploadResult.getPublicId())
                    .category(category)
                    .name(dishRequest.getName())
                    .basePrice(dishRequest.getBasePrice())
                    .build();
            dishRepository.save(dish);

            // 4. Tạo variant groups
            List<DishVariantGroup> dishVariantGroups = new ArrayList<>();
            List<DishVariantOption> dishVariantOptions = new ArrayList<>();
            for (DishVariantGroupRequest variantGroupRequest: dishRequest.getVariantGroups()) {
                DishVariantGroup variantGroup = DishVariantGroup.builder()
                        .dish(dish)
                        .groupName(variantGroupRequest.getGroupName())
                        .isRequired(variantGroupRequest.isRequired())
                        .isMultiple(variantGroupRequest.isMultiple())
                        .build();
                dishVariantGroups.add(variantGroup);

                // 4.1 Tạo variant options
                for (DishVariantOptionRequest variantOptionRequest: variantGroupRequest.getOptions()) {
                    DishVariantOption variantOption = DishVariantOption.builder()
                            .dishVariantGroup(variantGroup)
                            .optionName(variantOptionRequest.getOptionName())
                            .priceAdjustment(variantOptionRequest.getPriceAdjustment())
                            .build();
                    dishVariantOptions.add(variantOption);
                }
            }

            // 5. Lưu xuống DB
            dishVariantGroupRepository.saveAll(dishVariantGroups);
            dishVariantOptionRepository.saveAll(dishVariantOptions);
        } catch (Exception e) {
            if (uploadResult != null) {
                cloudinaryService.deleteFile(uploadResult.getPublicId()
                );
            }

            throw e;
        }
    }

    @Override
    public Page<DishResponse> getAllDishesByCategory(String categoryId, int page, int size) {
        // 1. Kiểm tra danh mục
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));
        }

        // 2. Tạo đối tượng phân trang
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 3. Query data
        Page<Dish> dishes = dishRepository.findAllDishesByCategory(categoryId, pageable);

        // 4. Mapping data trả về
        return dishes.map(dishMapper::mapToResponse);
    }

    @Override
    @Transactional
    public DishDetailResponse getDishDetail(String dishId) {
        // 1. Kiểm tra món ăn
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn không tồn tại"));

        // 2. Mapping data trả về
        return dishMapper.mapToResponseDetail(dish);
    }


    @Override
    @Transactional
    public void updateDish(String dishId, DishRequest request, MultipartFile file) {

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món ăn"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        // =========================
        // UPDATE DISH INFO
        // =========================

        dish.setName(request.getName());
        dish.setBasePrice(request.getBasePrice());
        dish.setCategory(category);

        // =========================
        // EXISTING GROUPS
        // =========================

        List<DishVariantGroup> existingGroups =
                dishVariantGroupRepository.findAllByDish_DishIdAndDeletedFalse(dishId);

        Map<String, DishVariantGroup> existingGroupMap =
                existingGroups.stream()
                        .collect(Collectors.toMap(
                                DishVariantGroup::getGroupId,
                                Function.identity()
                        ));

        Set<String> requestGroupIds = new HashSet<>();

        // =========================
        // LOOP REQUEST GROUPS
        // =========================

        if (request.getVariantGroups() != null) {

            for (DishVariantGroupRequest groupRequest : request.getVariantGroups()) {

                DishVariantGroup group;

                // =========================
                // UPDATE EXISTING GROUP
                // =========================

                if (groupRequest.getGroupId() != null) {

                    group = existingGroupMap.get(groupRequest.getGroupId());

                    if (group == null) {
                        throw new ResourceNotFoundException(
                                "Nhóm biến thể không tồn tại"
                        );
                    }

                    requestGroupIds.add(group.getGroupId());
                }

                // =========================
                // CREATE NEW GROUP
                // =========================

                else {

                    group = DishVariantGroup.builder()
                            .dish(dish)
                            .deleted(false)
                            .build();
                }

                group.setGroupName(groupRequest.getGroupName());
                group.setRequired(groupRequest.isRequired());
                group.setMultiple(groupRequest.isMultiple());

                dishVariantGroupRepository.save(group);

                // =========================
                // EXISTING OPTIONS
                // =========================

                List<DishVariantOption> existingOptions =
                        dishVariantOptionRepository.findAllByDishVariantGroup_GroupIdAndDeletedFalse(group.getGroupId());

                Map<String, DishVariantOption> existingOptionMap =
                        existingOptions.stream()
                                .collect(Collectors.toMap(
                                        DishVariantOption::getOptionId,
                                        Function.identity()
                                ));

                Set<String> requestOptionIds = new HashSet<>();

                // =========================
                // LOOP OPTIONS
                // =========================

                for (DishVariantOptionRequest optionRequest : groupRequest.getOptions()) {

                    DishVariantOption option;

                    // =========================
                    // UPDATE EXISTING OPTION
                    // =========================

                    if (optionRequest.getOptionId() != null) {

                        option = existingOptionMap.get(optionRequest.getOptionId());

                        if (option == null) {
                            throw new BadRequestException(
                                    "Lựa chọn biến thể không tồn tại"
                            );
                        }

                        requestOptionIds.add(option.getOptionId());
                    }

                    // =========================
                    // CREATE NEW OPTION
                    // =========================

                    else {

                        option = DishVariantOption.builder()
                                .dishVariantGroup(group)
                                .deleted(false)
                                .build();
                    }

                    option.setOptionName(optionRequest.getOptionName());
                    option.setPriceAdjustment(optionRequest.getPriceAdjustment());

                    dishVariantOptionRepository.save(option);
                }

                // =========================
                // SOFT DELETE MISSING OPTIONS
                // =========================

                for (DishVariantOption existingOption : existingOptions) {

                    if (!requestOptionIds.contains(existingOption.getOptionId())) {

                        existingOption.setDeleted(true);

                        dishVariantOptionRepository.save(existingOption);
                    }
                }
            }
        }

        // =========================
        // SOFT DELETE MISSING GROUPS
        // =========================

        for (DishVariantGroup existingGroup : existingGroups) {

            if (!requestGroupIds.contains(existingGroup.getGroupId())) {

                existingGroup.setDeleted(true);

                dishVariantGroupRepository.save(existingGroup);

                // inactive all child options

                List<DishVariantOption> options =
                        dishVariantOptionRepository.findAllByDishVariantGroup_GroupIdAndDeletedFalse(
                                existingGroup.getGroupId()
                        );

                for (DishVariantOption option : options) {

                    option.setDeleted(true);

                    dishVariantOptionRepository.save(option);
                }
            }
        }

        // Nếu có upload ảnh, upload lên cloudinary
        if (file != null) {
            // Xóa ảnh cũ
            if (dish.getPublicId() != null) {
                cloudinaryService.deleteFile(dish.getPublicId());
            }
            // Upload lên cloudinary
            validateFile.validateFileImage(file);
            UploadResult uploadResult = cloudinaryService.uploadFile(file);

            // Cập nhật vào dish
            dish.setPublicId(uploadResult.getPublicId());
            dish.setImage(uploadResult.getSecureUrl());
        }
        dishRepository.save(dish);
    }

    @Override
    public void updateDishStatus(String dishId) {
        // 1. Kiểm tra món ăn
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn không tồn tại"));

        // 2. Thay đổi trạng thái món ăn
        boolean newStatus = !dish.isStatus();
        dish.setStatus(newStatus);
        dishRepository.save(dish);



        // 3. Gửi data qua socket
        socketService.sendDishStatus(new DishStatusResponse(dishId, newStatus));
    }

    @Override
    public void deleteDish(String dishId) {
        // 1. Kiểm tra món ăn
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn không tồn tại"));

        // 2. Xóa mềm món ăn
        dish.setDeleted(true);
        dishRepository.save(dish);
    }

    @Override
    public List<DishResponse> getRecommendDishes(List<String> dishIds) {
        // Lấy tất cả các rules
        List<AssociationRule> rules = associationRuleRepository.findAll();

        // Đưa dishIds thành các phần tử độc lập
        Set<String> cartSet = new HashSet<>(dishIds);

        List<DishResponse> result = new ArrayList<>();

        for (AssociationRule rule: rules) {
            Set<String> antecedents =
                    rule.getItems()
                            .stream()
                            .filter(item -> item.getSide() == RuleSide.ANTECEDENT)
                            .map(item -> item.getDish().getDishId())
                            .collect(Collectors.toSet());

            boolean matched = cartSet.containsAll(antecedents);
            if (!matched) {
                continue;
            }

            rule.getItems()
                    .stream()
                    .filter(item -> item.getSide() == RuleSide.CONSEQUENT)
                    .filter(item -> !cartSet.contains(item.getDish().getDishId()))
                    .forEach(item -> {
                        Dish dish = item.getDish();

                        result.add(DishResponse.builder()
                                        .dishId(dish.getDishId())
                                        .name(dish.getName())
                                        .basePrice(dish.getBasePrice())
                                        .status(dish.isStatus())
                                        .image(dish.getImage())
                                .build());
                    });
        }


        return result;
    }
}
