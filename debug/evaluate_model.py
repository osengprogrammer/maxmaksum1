import os
import numpy as np
import tensorflow as tf
import cv2
from sklearn.metrics import roc_curve, auc
import matplotlib.pyplot as plt

# -----------------------------
# CONFIGURATION
# -----------------------------
# Exact path provided
MODEL_PATH = "/home/max/Downloads/New folder2/azura-main/app/src/main/assets/facenet.tflite"
INPUT_SIZE = (160, 160)

# -----------------------------
# HELPER FUNCTIONS
# -----------------------------

def preprocess(img):
    """
    Resizes and normalizes the image for the model.
    Model expects float32 input. 
    Standard FaceNet normalization is (pixel - 127.5) / 127.5 to get range [-1, 1].
    """
    img = cv2.resize(img, INPUT_SIZE)
    img = img.astype(np.float32)
    
    # Normalize to [-1, 1] (Standard for FaceNet)
    img = (img - 127.5) / 127.5
    return img

def get_embedding(interpreter, input_details, output_details, img):
    inp = preprocess(img)
    inp = np.expand_dims(inp, 0) # Add batch dimension: [1, 160, 160, 3]
    
    interpreter.set_tensor(input_details[0]['index'], inp)
    interpreter.invoke()
    
    emb = interpreter.get_tensor(output_details[0]['index'])
    emb = emb.flatten() # Flatten [1, 512] to [512]
    
    # Normalize embedding to unit length (Critical for L2/Cosine to work)
    norm = np.linalg.norm(emb)
    if norm == 0:
        return emb # avoid divide by zero
    return emb / norm

def l2_distance(u, v): 
    return np.linalg.norm(u - v)

def cosine_distance(u, v): 
    # Since vectors are unit-normalized, cosine distance = 1 - dot_product
    # Cosine Similarity = dot(u, v)
    return 1.0 - np.dot(u, v)

def load_img(path):
    if not os.path.exists(path):
        print(f"Warning: Image not found {path}")
        return np.zeros((160, 160, 3), dtype=np.uint8)
    # OpenCV loads as BGR, convert to RGB
    return cv2.cvtColor(cv2.imread(path), cv2.COLOR_BGR2RGB)

# -----------------------------
# SETUP TFLITE
# -----------------------------
if not os.path.exists(MODEL_PATH):
    print(f"❌ Error: Model file not found at {MODEL_PATH}")
    exit()

try:
    interp = tf.lite.Interpreter(model_path=MODEL_PATH)
    interp.allocate_tensors()
    
    input_details = interp.get_input_details()
    output_details = interp.get_output_details()
    
    print(f"✅ Model Loaded Successfully")
    print(f"   Input: {input_details[0]['shape']}")
    print(f"   Output: {output_details[0]['shape']}")

except Exception as e:
    print(f"❌ Failed to load model: {e}")
    exit()

# -----------------------------
# EVALUATION LOOP
# -----------------------------
pairs = []
labels = []

# Ensure data folders exist
if not os.path.exists("data/same") or not os.path.exists("data/diff"):
    print("❌ Data folders not found. Please run the setup_data.py script first!")
    exit()

print("Processing images...")

# Load pairs
for label_folder, label_val in [("same", 1), ("diff", 0)]:
    folder = f"data/{label_folder}"
    files = sorted(os.listdir(folder))
    
    # Process files in pairs (0&1, 2&3, etc.)
    for i in range(0, len(files) - 1, 2):
        path1 = os.path.join(folder, files[i])
        path2 = os.path.join(folder, files[i+1])
        
        img1 = load_img(path1)
        img2 = load_img(path2)
        
        pairs.append((img1, img2))
        labels.append(label_val)

distances_l2 = []
distances_cos = []

for idx, (img1, img2) in enumerate(pairs):
    e1 = get_embedding(interp, input_details, output_details, img1)
    e2 = get_embedding(interp, input_details, output_details, img2)
    
    d_l2 = l2_distance(e1, e2)
    d_cos = cosine_distance(e1, e2) # 0 means identical, 2 means opposite
    
    distances_l2.append(d_l2)
    distances_cos.append(d_cos)

distances_l2 = np.array(distances_l2)
distances_cos = np.array(distances_cos)
labels = np.array(labels)

# -----------------------------
# ANALYSIS & PLOTTING
# -----------------------------

# For L2: Small distance = Same Person (Positive)
# We invert distance for ROC because ROC expects "Higher Value = Positive Class"
fpr_l2, tpr_l2, thr_l2 = roc_curve(labels, -distances_l2)
roc_auc_l2 = auc(fpr_l2, tpr_l2)

# For Cosine Distance: Small distance (near 0) = Same Person
# We invert this too for standard ROC calculation
fpr_cos, tpr_cos, thr_cos = roc_curve(labels, -distances_cos)
roc_auc_cos = auc(fpr_cos, tpr_cos)

print("\n----------------RESULTS----------------")
print(f"AUC (L2 Distance):     {roc_auc_l2:.4f}")
print(f"AUC (Cosine Distance): {roc_auc_cos:.4f}")

# Find Best Threshold (Youden's J statistic)
best_idx_l2 = np.argmax(tpr_l2 - fpr_l2)
best_thresh_l2 = -thr_l2[best_idx_l2] # Re-invert to get actual distance

best_idx_cos = np.argmax(tpr_cos - fpr_cos)
best_thresh_cos = -thr_cos[best_idx_cos] # Re-invert

print(f"\nRecommended Thresholds:")
print(f"  L2 Distance:      {best_thresh_l2:.4f} (If dist < {best_thresh_l2:.4f}, it's a match)")
print(f"  Cosine Distance:  {best_thresh_cos:.4f} (If dist < {best_thresh_cos:.4f}, it's a match)")

# Plot
plt.figure(figsize=(8, 6))
plt.plot(fpr_l2, tpr_l2, label=f"L2 (AUC={roc_auc_l2:.2f})", linewidth=2)
plt.plot(fpr_cos, tpr_cos, label=f"Cosine (AUC={roc_auc_cos:.2f})", linewidth=2)
plt.plot([0, 1], [0, 1], 'k--', alpha=0.5)
plt.xlabel("False Positive Rate")
plt.ylabel("True Positive Rate")
plt.title("Face Recognition Performance (ROC)")
plt.legend(loc="lower right")
plt.grid(True)
plt.show()