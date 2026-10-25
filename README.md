---

## 📦 Tài nguyên Thực nghiệm & Google Drive Archive

Do kích thước tập dữ liệu lớn và các tệp sổ tay Jupyter Notebook chứa đầy đủ biểu đồ trực quan hóa có dung lượng cao, toàn bộ tài nguyên nghiên cứu, trọng số mô hình và nhật ký kiểm thử đã được lưu trữ công khai tại Google Drive:

[![Google Drive Folder](https://img.shields.io/badge/Google%20Drive-Tải%20Toàn%20Bộ%20Thực%20Nghiệm-4285F4?style=for-the-badge&logo=googledrive&logoColor=white)](https://drive.google.com/drive/folders/YOUR_DRIVE_FOLDER_ID_HERE?usp=sharing)
[![Open In Colab](https://img.shields.io/badge/Colab-Chạy%20Thực%20Nghiệm%20Trực%20Tuyến-F9AB00?style=for-the-badge&logo=googlecolab&logoColor=white)](https://colab.research.google.com/drive/YOUR_COLAB_NOTEBOOK_ID_HERE)

> 📌 **Lưu ý truy cập:** Thư mục Drive được thiết lập ở chế độ **Public (Ai có liên kết đều có thể xem và tải về)**.

### Danh mục tệp lưu trữ trên Drive:

| Thư mục / Tệp tin | Định dạng | Dung lượng | Nội dung mô tả |
| :--- | :---: | :---: | :--- |
| `01_Notebooks/Spotify_Recommender_Model_Comparison.ipynb` | `.ipynb` | ~15 MB | Sổ tay thực nghiệm đầy đủ: Pipeline nạp dữ liệu, biểu đồ phân phối, 5-Fold CV, SHAP analysis và trực quan hóa kết quả so sánh 6 mô hình. |
| `01_Notebooks/Optuna_Hyperparameter_Tuning.ipynb` | `.ipynb` | ~8 MB | Toàn bộ quá trình dò tìm không gian siêu tham số (TPE Bayesian Optimization) cho GBM, XGBoost, LightGBM và MLP. |
| `02_Datasets/community_signal_daily_full.csv` | `.csv` | ~165 MB | Bảng tín hiệu chuyển tiếp đầy đủ (1.048.576 cặp chuyển tiếp hợp lệ $C_{trans} \ge 2$ trích xuất từ 1.000.000 playlist). |
| `02_Datasets/community_signal_daily_features_clean.csv` | `.csv` | ~2.5 MB | Tập mẫu chuẩn hóa 5.000 dòng đại diện dùng để huấn luyện và kiểm thử chéo ngoại tuyến. |
| `02_Datasets/songs_metadata.csv` | `.csv` | ~1.2 MB | Danh mục thông tin bài hát (Track ID, Name, Artist, Album, Popularity, Audio Preview URL). |
| `03_Trained_Models/lightgbm_model2_tuned.pkl` | `.pkl` | ~18 MB | Trọng số mô hình LightGBM tối ưu đã huấn luyện, sẵn sàng phục vụ cho REST API serving. |
| `03_Trained_Models/stacking_ensemble_m2.pkl` | `.pkl` | ~45 MB | Trọng số mô hình Stacking Ensemble (LGBM + XGB + RF + MLP + Ridge). |
| `04_Logs/benchmark_metrics_raw.csv` | `.csv` | < 1 MB | Bảng ghi chi tiết các chỉ số $R^2$, MSE, MAE, NDCG@10 theo từng Fold của từng thuật toán. |

---
