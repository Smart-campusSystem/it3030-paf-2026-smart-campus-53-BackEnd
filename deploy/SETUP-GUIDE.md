# Smart Campus System — Complete Manual Setup Guide

> [!IMPORTANT]
> Follow these steps **in order**. Each step depends on outputs from previous steps (IDs, ARNs, endpoints). Keep a notepad open to save values as you go.

---

## 📋 Values You'll Collect Along the Way

Keep track of these as you create resources — you'll need them later:

| Value | Where You Get It | Example |
|---|---|---|
| AWS Account ID | Step 1 | `123456789012` |
| IAM Access Key ID | Step 3 | `AKIAIOSFODNN7EXAMPLE` |
| IAM Secret Access Key | Step 3 | `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY` |
| SNS Topic ARN | Step 4 | `arn:aws:sns:ap-south-1:123456789012:smart-campus-billing` |
| EC2 Key Pair name | Step 6 | `smart-campus-key` |
| EC2 Elastic IP | Step 6 | `13.233.xx.xx` |
| EC2 Instance ID | Step 6 | `i-0abc123def456` |
| RDS Endpoint | Step 7 | `smart-campus-db.cxxxxx.ap-south-1.rds.amazonaws.com` |
| RDS Master Password | Step 7 | (you choose) |
| Frontend S3 Bucket Name | Step 8 | `smart-campus-frontend-chanuka` |
| CloudFront Distribution ID | Step 9 | `E1A2B3C4D5E6F7` |
| CloudFront Domain | Step 9 | `d1234abcdef.cloudfront.net` |

---

## Step 1: Create AWS Account

> [!CAUTION]
> AWS requires a credit/debit card. You will NOT be charged as long as you stay within the free tier limits described in this guide.

1. Go to **https://aws.amazon.com/** → Click **"Create an AWS Account"**
2. Enter your **email address** and choose an **account name** (e.g., `smart-campus-chanuka`)
3. Verify your email with the code sent to your inbox
4. Set a **root password** (save it securely)
5. Choose **"Personal"** account type
6. Enter your **contact information** and **payment method** (credit/debit card)
7. **Identity verification**: AWS will call/text you with a code
8. Select **"Basic Support - Free"** plan
9. Click **"Complete Sign Up"**
10. Sign in to the **AWS Management Console**: https://console.aws.amazon.com/

**📝 Save**: Your **12-digit Account ID** (visible in the top-right corner dropdown → "Account ID")

---

## Step 2: Enable Free Tier Alerts & Billing Access

> [!WARNING]
> Do this BEFORE creating any resources. This is your safety net against surprise charges.

### 2.1 Enable Billing Alerts

1. Sign in as **root user** (the email you used to create the account)
2. Click your **account name** (top-right corner) → **"Account"**
3. Scroll down to **"Billing preferences"** (or go to **Billing** → **Billing preferences**)
4. Check ✅ **"Receive AWS Free Tier alerts"**
5. Enter your **email address** for alerts (optional — defaults to your account email if left blank)
6. Check ✅ **"Receive CloudWatch billing alerts"** (once enabled, this cannot be disabled)
7. Click **"Update"**

### 2.2 Enable IAM User Billing Access

1. Still on the Account page, find **"IAM user and role access to Billing information"**
2. Click **"Edit"**
3. Check ✅ **"Activate IAM Access"**
4. Click **"Update"**

---

## Step 3: Create IAM User (Never Use Root for Daily Work)

1. Go to **IAM Console**: https://console.aws.amazon.com/iam/
2. Left sidebar → **"Users"** → **"Create user"**
3. **User name**: `smart-campus-deployer`
4. Check ✅ **"Provide user access to the AWS Management Console"** (optional, for you to log in)
5. Choose **"I want to create an IAM user"**
6. Set a **console password** → Uncheck "User must create new password at next sign-in"
7. Click **"Next"**

### 3.1 Attach Policy

8. Choose **"Attach policies directly"**
9. Click **"Create policy"** (opens new tab)
10. Click the **"JSON"** tab
11. Paste the contents of your `iam-policy.json` — specifically the `"policy"` object inside it:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "S3FrontendDeployment",
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::smart-campus-frontend-*"
      ]
    },
    {
      "Sid": "S3ProfileImages",
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject"
      ],
      "Resource": "arn:aws:s3:::smart-campus-images-b2/*"
    },
    {
      "Sid": "S3ProfileImagesList",
      "Effect": "Allow",
      "Action": "s3:ListBucket",
      "Resource": "arn:aws:s3:::smart-campus-images-b2"
    },
    {
      "Sid": "CloudFrontInvalidation",
      "Effect": "Allow",
      "Action": "cloudfront:CreateInvalidation",
      "Resource": "*"
    },
    {
      "Sid": "CloudWatchReadOnly",
      "Effect": "Allow",
      "Action": [
        "cloudwatch:GetMetricData",
        "cloudwatch:DescribeAlarms"
      ],
      "Resource": "*"
    }
  ]
}
```

> [!NOTE]
> We use `"Resource": "*"` for CloudFront temporarily. After you create the distribution in Step 9, come back and narrow it to `arn:aws:cloudfront::<ACCOUNT-ID>:distribution/<DIST-ID>`.

12. Click **"Next"**
13. **Policy name**: `SmartCampusDeployerPolicy`
14. Click **"Create policy"**
15. Go back to the **Create user** tab
16. Click the **refresh** icon (🔄) next to the policy search
17. Search for `SmartCampusDeployerPolicy` → Check ✅ it
18. Also search and attach: `AmazonEC2ReadOnlyAccess` (for monitoring)
19. Click **"Next"** → **"Create user"**

### 3.2 Create Access Keys (for Jenkins & Backend)

20. Click on the user **`smart-campus-deployer`**
21. Go to **"Security credentials"** tab
22. Scroll down to **"Access keys"** → **"Create access key"**
23. Choose **"Command Line Interface (CLI)"**
24. Check ✅ the acknowledgement → Click **"Next"**
25. **Description**: `Jenkins and backend deployment`
26. Click **"Create access key"**

> [!CAUTION]
> **SAVE BOTH VALUES NOW!** The Secret Access Key is shown only once:
> - **Access Key ID**: `AKIA...` → 📝 Save this
> - **Secret Access Key**: `wJal...` → 📝 Save this
>
> Click **"Download .csv file"** as backup. Store it securely, NEVER commit to Git.

---

## Step 4: Create SNS Topic for Alarm Notifications

1. Go to **SNS Console**: https://ap-south-1.console.aws.amazon.com/sns/
2. Make sure region is **Asia Pacific (Mumbai) ap-south-1** (top-right dropdown)
3. Left sidebar → **"Topics"** → **"Create topic"**
4. **Type**: Standard
5. **Name**: `smart-campus-billing`
6. **Display name**: `SmartCampus Alerts`
7. Leave everything else default → Click **"Create topic"**

**📝 Save**: The **Topic ARN** shown at the top (e.g., `arn:aws:sns:ap-south-1:123456789012:smart-campus-billing`)

### 4.1 Subscribe Your Email

8. On the topic page → Click **"Create subscription"**
9. **Protocol**: Email
10. **Endpoint**: `your-actual-email@gmail.com`
11. Click **"Create subscription"**
12. **CHECK YOUR EMAIL** → You'll receive a confirmation email from AWS → Click **"Confirm subscription"**
13. The subscription status should change from "Pending confirmation" to **"Confirmed"**

---

## Step 5: Set Up Billing Alarms

### 5.1 AWS Budget — Monthly Cost Alert ($1)

1. Go to **Billing Console**: https://console.aws.amazon.com/billing/
2. Left sidebar → **"Budgets"** → **"Create a budget"**
3. Choose **"Customize (advanced)"**
4. **Budget type**: Cost budget → Click **"Next"**
5. **Budget name**: `SmartCampus-Monthly`
6. **Period**: Monthly
7. **Budget renewal type**: Recurring budget
8. **Budgeting method**: Fixed
9. **Enter your budgeted amount**: `1.00` (USD)
10. Click **"Next"**

### Configure alerts (add 3 thresholds):

11. **Alert 1**: Click "Add an alert threshold"
    - **Threshold**: `50`%
    - **Trigger**: Actual
    - **Email recipients**: `your-email@gmail.com`

12. **Alert 2**: Click "Add an alert threshold"
    - **Threshold**: `80`%
    - **Trigger**: Actual
    - **Email recipients**: `your-email@gmail.com`

13. **Alert 3**: Click "Add an alert threshold"
    - **Threshold**: `100`%
    - **Trigger**: Actual
    - **Email recipients**: `your-email@gmail.com`

14. Click **"Next"** → **"Next"** → **"Create budget"**

### 5.2 AWS Budget — Forecasted Cost Alert ($5)

15. **"Create a budget"** again
16. Same settings as above, except:
    - **Budget name**: `SmartCampus-Forecast`
    - **Amount**: `5.00`
    - **Alert threshold**: `100`%
    - **Trigger**: **Forecasted** (not Actual)
17. Click through to **"Create budget"**

### 5.3 CloudWatch Billing Alarm (EstimatedCharges > $1)

> [!IMPORTANT]
> Billing metrics are ONLY available in **us-east-1** (N. Virginia). You must switch regions for this alarm.

18. Switch region to **US East (N. Virginia)** using the dropdown in the top-right
19. Go to **CloudWatch**: https://us-east-1.console.aws.amazon.com/cloudwatch/
20. Left sidebar → **"Alarms"** → **"All alarms"** → **"Create alarm"**
21. Click **"Select metric"**
22. Click **"Billing"** → **"Total Estimated Charge"**
23. Check ✅ **"EstimatedCharges"** (Currency: USD) → Click **"Select metric"**
24. **Statistic**: Maximum
25. **Period**: 6 hours
26. **Threshold type**: Static
27. **Whenever EstimatedCharges is**: Greater than → `1`
28. Click **"Next"**
29. **Send notification to**: Select **"Create new topic"** or use existing
    - If creating new: Topic name `smart-campus-billing-useast1`, email: yours
    - **OR** use the SNS topic ARN from Step 4 (but note it's in ap-south-1, you may need a us-east-1 topic for billing)
30. Click **"Next"**
31. **Alarm name**: `SmartCampus-BillingAlarm`
32. Click **"Next"** → **"Create alarm"**
33. If you created a new SNS topic, **check your email and confirm the subscription**
34. **Switch back to ap-south-1** region!

---

## Step 6: Launch EC2 Instance

### 6.1 Create Key Pair

1. Go to **EC2 Console**: https://ap-south-1.console.aws.amazon.com/ec2/
2. Verify region is **ap-south-1** (Mumbai)
3. Left sidebar → **"Key Pairs"** (under Network & Security) → **"Create key pair"**
4. **Name**: `smart-campus-key`
5. **Key pair type**: RSA
6. **Private key file format**: `.pem`
7. Click **"Create key pair"**
8. The `.pem` file downloads automatically

> [!CAUTION]
> **SAVE THIS FILE SECURELY!** Move it to `C:\Users\chanuka\.ssh\smart-campus-key.pem`. You cannot download it again.

### 6.2 Create Security Groups

9. Left sidebar → **"Security Groups"** (under Network & Security) → **"Create security group"**

**Security Group 1: EC2**
- **Name**: `smart-campus-ec2-sg`
- **Description**: `EC2 backend server`
- **VPC**: (default VPC)
- **Inbound rules** → Click "Add rule" for each:

| Type | Port Range | Source | Description |
|---|---|---|---|
| SSH | 22 | My IP | SSH from your IP only |
| Custom TCP | 8080 | Anywhere-IPv4 (0.0.0.0/0) | Spring Boot API |

- **Outbound rules**: Leave default (All traffic)
- Click **"Create security group"**

10. Create **Security Group 2: RDS**
- **Name**: `smart-campus-rds-sg`
- **Description**: `RDS MySQL from EC2 only`
- **VPC**: (default VPC)
- **Inbound rules**:

| Type | Port Range | Source | Description |
|---|---|---|---|
| MySQL/Aurora | 3306 | Custom → search for `smart-campus-ec2-sg` | MySQL from EC2 |

- Click **"Create security group"**

### 6.3 Launch EC2 Instance

11. Left sidebar → **"Instances"** → **"Launch instances"**
12. **Name**: `smart-campus-backend`
13. **Application and OS Images**: 
    - **Amazon Linux 2023** AMI (should be the default, shows "Free tier eligible")
14. **Instance type**: `t2.micro` — shows "Free tier eligible" ✅
15. **Key pair**: Select `smart-campus-key`
16. **Network settings** → Click **"Edit"**:
    - **VPC**: Default
    - **Subnet**: No preference
    - **Auto-assign public IP**: **Enable**
    - **Firewall**: Select existing security group → Pick `smart-campus-ec2-sg`
17. **Configure storage**: `8` GiB, `gp3`
18. Click **"Launch instance"**
19. Click on the instance ID link to go to its detail page
20. Wait for **Instance state** to show **"Running"** and **Status check** to show **"2/2 checks passed"**

**📝 Save**: The **Instance ID** (e.g., `i-0abc123def456`)

### 6.4 Allocate and Associate Elastic IP

21. Left sidebar → **"Elastic IPs"** (under Network & Security) → **"Allocate Elastic IP address"**
22. Leave defaults → Click **"Allocate"**
23. Select the new Elastic IP → **"Actions"** → **"Associate Elastic IP address"**
24. **Resource type**: Instance
25. **Instance**: Select `smart-campus-backend`
26. Click **"Associate"**

**📝 Save**: The **Elastic IP** address (e.g., `13.233.xx.xx`)

> [!WARNING]
> Elastic IP is **free only while associated with a running instance**. If you stop EC2 or detach the EIP, you'll be charged ~$3.60/month. If you're done testing, **release** the EIP.

### 6.5 SSH into EC2 and Run Setup

27. Open **Git Bash** or **PowerShell** on your Windows machine:

```bash
# Upload the setup script
scp -i "C:/Users/chanuka/.ssh/smart-campus-key.pem" ^
  "d:/PAF/smart-campus-system-BackEnd/deploy/ec2-setup.sh" ^
  ec2-user@<ELASTIC-IP>:/tmp/ec2-setup.sh

# SSH into the instance
ssh -i "C:/Users/chanuka/.ssh/smart-campus-key.pem" ec2-user@<ELASTIC-IP>
```

28. On the **EC2 instance** (in the SSH session):

```bash
chmod +x /tmp/ec2-setup.sh
sudo /tmp/ec2-setup.sh
```

29. The script installs everything and creates the env template. Verify:

```bash
java -version       # Should show Java 21
redis6-cli ping     # Should show PONG
sudo systemctl status smart-campus  # Should show "inactive" (no JAR yet)
```

### 6.6 Create CloudWatch Alarms for EC2

30. Go to **CloudWatch Console**: https://ap-south-1.console.aws.amazon.com/cloudwatch/

**Alarm 1: EC2 High CPU**
31. Left sidebar → **"Alarms"** → **"Create alarm"**
32. **"Select metric"** → **EC2** → **Per-Instance Metrics**
33. Filter by your instance ID → Check ✅ **CPUUtilization** → **"Select metric"**
34. **Statistic**: Average
35. **Period**: 5 minutes
36. **Threshold**: Greater than `85`
37. Click **"Next"**
38. **Notification**: Select existing SNS topic → `smart-campus-billing`
39. Click **"Next"**
40. **Alarm name**: `SmartCampus-EC2-HighCPU`
41. **"Create alarm"**

**Alarm 2: EC2 Status Check**
42. Repeat: **"Create alarm"** → EC2 → Per-Instance → your instance → **StatusCheckFailed**
43. **Statistic**: Maximum, **Period**: 5 min, **Threshold**: Greater than `0`
44. **Alarm name**: `SmartCampus-EC2-StatusCheck`

---

## Step 7: Create RDS MySQL Instance

1. Go to **RDS Console**: https://ap-south-1.console.aws.amazon.com/rds/
2. Click **"Create database"**
3. **Choose a database creation method**: Standard create
4. **Engine type**: MySQL
5. **Engine version**: MySQL 8.0.x (latest 8.0 available)
6. **Templates**: Click **"Free tier"** ✅

7. **Settings**:
    - **DB instance identifier**: `smart-campus-db`
    - **Master username**: `admin`
    - **Credentials management**: Self managed
    - **Master password**: Type a strong password → 📝 **SAVE THIS!**
    - **Confirm master password**: Re-type it

8. **Instance configuration**: `db.t3.micro` (auto-selected by Free tier)

9. **Storage**:
    - **Storage type**: gp2
    - **Allocated storage**: `20` GiB
    - ❌ **UNCHECK** "Enable storage autoscaling"

> [!WARNING]
> **Disable storage autoscaling!** If left enabled, AWS auto-grows beyond 20 GB free tier and charges you.

10. **Connectivity**:
    - **Compute resource**: Don't connect to an EC2 compute resource (we'll use security groups)
    - **VPC**: Default VPC
    - **Public access**: **No**
    - **VPC security group**: Choose existing → Select `smart-campus-rds-sg`
    - **Remove** the default security group if also selected

11. **Database authentication**: Password authentication

12. Expand **"Additional configuration"**:
    - **Initial database name**: `smartcampus`
    - **Enable automated backups**: ✅ Yes
    - **Backup retention period**: `1` day (change to `0` or disable if restricted by your account type)
    - ❌ **UNCHECK** "Enable Enhanced Monitoring"
    - ✅ Check "Enable auto minor version upgrade"
    - ✅ Check "Enable deletion protection"

13. Click **"Create database"**
14. Wait **5-10 minutes** for status to become **"Available"**

15. Click on `smart-campus-db` → Under **"Connectivity & security"**:

**📝 Save**: The **Endpoint** (e.g., `smart-campus-db.cxxxxxxxxx.ap-south-1.rds.amazonaws.com`)

### 7.1 Create CloudWatch Alarms for RDS

16. Go to **CloudWatch** → **"Create alarm"**

**Alarm: RDS High CPU**
- Metric: **RDS** → **DBInstanceIdentifier** → Filter or search `smart-campus-db` → Check ✅ **CPUUtilization**
- **Period**: 5 minutes, **Threshold**: Greater than `80`
- **SNS topic**: `smart-campus-billing`
- **Name**: `SmartCampus-RDS-HighCPU`

**Alarm: RDS Low Storage**
- Metric: **RDS** → **DBInstanceIdentifier** → Filter or search `smart-campus-db` → Check ✅ **FreeStorageSpace**
- **Period**: 5 minutes
- **Threshold**: Less than `2147483648` (this is 2 GB in bytes)
- **Name**: `SmartCampus-RDS-LowStorage`

---

## Step 8: Create S3 Buckets

### 8.1 Frontend Bucket

1. Go to **S3 Console**: https://s3.console.aws.amazon.com/s3/
2. Click **"Create bucket"**
3. **Bucket name**: `smart-campus-frontend-chanuka` (must be globally unique)
4. **Region**: Asia Pacific (Mumbai) ap-south-1
5. **Object Ownership**: ACLs disabled (recommended)
6. ✅ **Block ALL public access** (CloudFront accesses via OAC, not public)
7. **Bucket versioning**: Disable
8. Click **"Create bucket"**

**📝 Save**: Bucket name

### 8.2 Profile Images Bucket

If `smart-campus-images-b2` doesn't exist yet:

9. **"Create bucket"** → Name: `smart-campus-images-b2` → Same settings as above
10. Click **"Create bucket"**

---

## Step 9: Create CloudFront Distribution

1. Go to **CloudFront Console**: https://console.aws.amazon.com/cloudfront/
2. Click **"Create distribution"**
3. **Step 1: Get started**:
    - **Distribution name**: `smart-campus-frontend`
    - **Description**: `Smart Campus System`
    - **Distribution type**: Select **"Single website or app"**
    - **Domain**: Leave blank (optional)
    - Click **"Next"**
4. **Step 2: Specify origin**:
    - **S3 origin**: Click the input box or click **"Browse S3"** and select your frontend S3 bucket (e.g., `smart-campus-frontend-chanuka`)
    - Under **"Allow private S3 bucket access to CloudFront"**: Check ✅ **"Allow private S3 bucket access to CloudFront - Recommended"**
    - **Origin settings**: Select **"Use recommended origin settings"** (AWS will automatically manage OAC settings)
    - **Cache settings**: Select **"Use recommended cache settings tailored to serving S3 content"**
    - Click **"Next"**
5. **Step 3: Enable security**:
    - Under **Web Application Firewall (WAF)**: Select **"Do not enable security protections"** (to avoid WAF charges)
    - Click **"Next"**
6. **Step 4: Review and create**:
    - Click the orange **"Create distribution"** button at the bottom.

> [!IMPORTANT]
> **Configure Price Class & Default Root Object (Post-Creation Settings)**:
> In newer AWS accounts, the creation wizard has been simplified and does not display **Price class** or **Default root object** settings during the creation steps.
> To configure these (highly recommended to reduce costs and avoid 403 Access Denied errors):
> 1. Wait for the distribution status to change from **Deploying** to **Deployed** (usually takes 5–15 minutes).
> 2. Click on the **Distribution ID** from the list to open its details.
> 3. Under the **"General"** tab, locate the **"Settings"** or **"General configuration"** box and click **"Edit"** on the right side.
> 4. **Price class**: Choose **"Use only North America and Europe"** (this is a project deployment, so limiting edge locations reduces cost).
> 5. **Default root object**: Type `index.html` ⚠️ *(CRITICAL: without this, visiting the root URL `https://xxxx.cloudfront.net/` will return a **403 Access Denied** error).*
> 6. Scroll down and click **"Save changes"**.


### 9.5 Update S3 Bucket Policy (CRITICAL)

16. In your CloudFront distribution details page, a **yellow/blue banner** will appear at the top saying: *"S3 bucket policy needs to be updated. Copy the policy and update it in S3."*
    - **If you do not see this banner at the top:**
      - Go to the **"Origins"** tab in your distribution.
      - Select the S3 origin (e.g., `smart-campus-frontend-chanuka...`) and click **"Edit"**.
      - Scroll down to the **"Origin access"** section. You will see a box containing the message that the S3 bucket policy needs to be updated.
17. Click the **"Copy policy"** button.
18. Go to the **S3 Console** → open the `smart-campus-frontend-chanuka` bucket → select the **"Permissions"** tab.
19. Scroll down to the **"Bucket policy"** section and click **"Edit"**.
20. **Paste** the copied JSON policy into the editor and click **"Save changes"**.

### 9.6 Add Origin 2: Backend EC2

21. In your CloudFront distribution → **"Origins"** tab → **"Create origin"**
22. **Origin domain**: Paste your EC2 instance's **Public IPv4 DNS** (e.g., `ec2-3-109-188-213.ap-south-1.compute.amazonaws.com`).
    - ⚠️ *Note: AWS CloudFront does not accept raw IP addresses (like `3.109.188.213`) as the origin domain. You must use the DNS name.*
    - **How to find it:** Go to the **EC2 Console** → select your running instance → in the **Details** tab below, copy the **Public IPv4 DNS**.
23. **Protocol**: HTTP only
24. **HTTP port**: `8080`
25. Leave HTTPS port as 443 (unused since we selected HTTP only)
26. **Name**: `EC2-smart-campus-backend`
27. Click **"Create origin"**

### 9.7 Add Origin 3: Images S3

28. **"Create origin"** again
29. **Origin domain**: Select `smart-campus-images-b2` from dropdown
30. **Origin access**: OAC → Select the OAC you created earlier OR create new
31. **Name**: `S3-smart-campus-images`
32. Click **"Create origin"**
33. **Update the images S3 bucket policy** — same process as Step 9.5:
    - Copy the policy from the banner
    - Go to S3 → `smart-campus-images-b2` → Permissions → Bucket policy → Paste & Save

### 9.8 Create Cache Behaviors

Go to **"Behaviors"** tab

**Behavior 1: `/api/*`** → Click **"Create behavior"**

| Field | Value |
|---|---|
| Path pattern | `/api/*` |
| Origin and origin group | `EC2-smart-campus-backend` |
| Viewer protocol policy | Redirect HTTP to HTTPS |
| Allowed HTTP methods | GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE |
| Cache key and origin requests → Cache policy | **CachingDisabled** |
| Origin request policy | **AllViewerExceptHostHeader** |

Click **"Create behavior"**

**Behavior 2: `/oauth2/*`** → **"Create behavior"**

| Field | Value |
|---|---|
| Path pattern | `/oauth2/*` |
| Origin and origin group | `EC2-smart-campus-backend` |
| Viewer protocol policy | Redirect HTTP to HTTPS |
| Allowed HTTP methods | GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE |
| Cache policy | **CachingDisabled** |
| Origin request policy | **AllViewerExceptHostHeader** |

Click **"Create behavior"**

**Behavior 3: `/login/oauth2/*`** → **"Create behavior"**

| Field | Value |
|---|---|
| Path pattern | `/login/oauth2/*` |
| Origin and origin group | `EC2-smart-campus-backend` |
| Viewer protocol policy | Redirect HTTP to HTTPS |
| Allowed HTTP methods | GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE |
| Cache policy | **CachingDisabled** |
| Origin request policy | **AllViewerExceptHostHeader** |

Click **"Create behavior"**

**Behavior 4: `/Profile-images/*`** → **"Create behavior"**

| Field | Value |
|---|---|
| Path pattern | `/Profile-images/*` |
| Origin and origin group | `S3-smart-campus-images` |
| Viewer protocol policy | Redirect HTTP to HTTPS |
| Allowed HTTP methods | GET, HEAD |
| Cache policy | **CachingOptimized** |
| Compress objects automatically | Yes |

Click **"Create behavior"**

### 9.9 Create Custom Error Responses (SPA Fallback)

34. Go to **"Error pages"** tab → **"Create custom error response"**

**Error Response 1:**

| Field | Value |
|---|---|
| HTTP error code | 403: Forbidden |
| Customize error response | Yes |
| Response page path | `/index.html` |
| HTTP response code | 200: OK |
| Error caching minimum TTL (seconds) | 10 |

Click **"Create custom error response"**

**Error Response 2:** → **"Create custom error response"** again

| Field | Value |
|---|---|
| HTTP error code | 404: Not Found |
| Customize error response | Yes |
| Response page path | `/index.html` |
| HTTP response code | 200: OK |
| Error caching minimum TTL (seconds) | 10 |

> [!IMPORTANT]
> These error responses are **critical** for React Router to work! Without them, navigating directly to `https://xxx.cloudfront.net/bookings` returns a 403 from S3 instead of loading the app.

35. **📝 Save** from the distribution's **"General"** tab:
    - **Distribution ID** (e.g., `E1A2B3C4D5E6F7`)
    - **Distribution domain name** (e.g., `d1234abcdef.cloudfront.net`)

36. Wait for **Status** to change from "Deploying" to **"Enabled"** (~5-15 minutes)

---

## Step 10: Configure Backend Environment on EC2

1. SSH into your EC2:

```bash
ssh -i "C:/Users/chanuka/.ssh/smart-campus-key.pem" ec2-user@<ELASTIC-IP>
```

2. Create and edit the environment file:

```bash
sudo cp /opt/smart-campus/.env.template /opt/smart-campus/.env
sudo chmod 600 /opt/smart-campus/.env
sudo nano /opt/smart-campus/.env
```

3. Fill in ALL values using your saved notes. Replace every `<placeholder>`:

```bash
# Spring Boot
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# MySQL (RDS)
MYSQL_HOST=smart-campus-db.cxxxxxxxxx.ap-south-1.rds.amazonaws.com
MYSQL_PORT=3306
MYSQL_DB=smartcampus
MYSQL_USER=admin
MYSQL_PASSWORD=<your-rds-master-password-from-step-7>

# JWT (generate: python3 -c "import secrets; print(secrets.token_hex(32))")
APP_JWT_SECRET=<paste-64-char-hex-string>
APP_JWT_EXPIRATION_MS=86400000

# AWS S3
AWS_ACCESS_KEY_ID=<access-key-from-step-3>
AWS_SECRET_ACCESS_KEY=<secret-key-from-step-3>
AWS_S3_PROFILE_BUCKET=smart-campus-images-b2
AWS_S3_PROFILE_REGION=ap-south-1
AWS_S3_PROFILE_PUBLIC_BASE_URL=https://<cloudfront-domain>.cloudfront.net/

# Google OAuth2
GOOGLE_CLIENT_ID=<your-google-client-id>
GOOGLE_CLIENT_SECRET=<your-google-client-secret>
APP_OAUTH2_FRONTEND_REDIRECT_URL=https://<cloudfront-domain>.cloudfront.net/auth/callback

# Mail
SPRING_MAIL_USERNAME=<your-email@gmail.com>
SPRING_MAIL_PASSWORD=<your-gmail-app-password>

# Redis (local)
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
```

4. Save: **Ctrl+O** → **Enter** → **Ctrl+X**

> [!IMPORTANT]
> **First-time schema creation**: The prod profile uses `ddl-auto=validate`, so Hibernate won't create tables. For the **first run only**, add this line to `.env`:
> ```
> SPRING_JPA_HIBERNATE_DDL_AUTO=update
> ```
> After Spring Boot starts successfully and creates tables, **remove this line** and restart the service. The tables persist in RDS.

---

## Step 11: Update Google OAuth2 Redirect URI

1. Go to **Google Cloud Console**: https://console.cloud.google.com/
2. Select your project
3. Navigate to **APIs & Services** → **Credentials**
4. Click on your **OAuth 2.0 Client ID**
5. Under **"Authorized redirect URIs"**, click **"ADD URI"** and add:
   ```
   https://<cloudfront-domain>.cloudfront.net/auth/callback
   ```
6. Under **"Authorized JavaScript origins"**, click **"ADD URI"** and add:
   ```
   https://<cloudfront-domain>.cloudfront.net
   ```
7. Click **"Save"**

> [!TIP]
> Keep existing `http://localhost:5173` entries for local development.

---

## Step 12: First Manual Deployment (Test Before Setting Up Jenkins)

### 12.1 Deploy Backend

On your **local Windows machine** (PowerShell or Git Bash):

```powershell
# 1. Build the JAR
cd "d:\PAF\smart-campus-system-BackEnd"
.\mvnw.cmd package -DskipTests -B

# 2. Find the JAR name
dir target\*.jar
# You should see something like: demo-0.0.1-SNAPSHOT.jar

# 3. Upload to EC2
scp -i "C:\Users\chanuka\.ssh\smart-campus-key.pem" `
  "target\demo-0.0.1-SNAPSHOT.jar" `
  ec2-user@<ELASTIC-IP>:/tmp/app.jar
```

Then on **EC2** (SSH session):

```bash
# Move JAR and set ownership
sudo mv /tmp/app.jar /opt/smart-campus/app.jar
sudo chown smartcampus:smartcampus /opt/smart-campus/app.jar

# Start the service
sudo systemctl start smart-campus

# Watch the startup logs (wait ~30-60 seconds)
sudo journalctl -u smart-campus -f
```

Wait until you see `Started DemoApplication in X seconds`. Then test:

```bash
curl http://localhost:8080/api/health
```

✅ Expected:
```json
{"status":"UP","timestamp":"...","service":"smart-campus-backend","database":"UP","redis":"UP"}
```

> [!WARNING]
> If `database` shows `DOWN`, check:
> - RDS security group allows 3306 from EC2 SG
> - `.env` has the correct RDS endpoint and password
> - RDS instance is in "Available" status

### 12.2 Deploy Frontend

On your **local Windows machine**:

```powershell
# 1. Build the frontend
cd "d:\PAF\smart-campus-system-FrontEnd"
npm run build

# 2. Configure AWS CLI (one-time)
aws configure
#   AWS Access Key ID: <from step 3>
#   AWS Secret Access Key: <from step 3>
#   Default region: ap-south-1
#   Default output format: json

# 3. Upload to S3
aws s3 sync dist/ s3://smart-campus-frontend-chanuka/ --delete

# 4. Invalidate CloudFront cache
aws cloudfront create-invalidation `
  --distribution-id <YOUR-DISTRIBUTION-ID> `
  --paths "/*"
```

> [!NOTE]
> If `aws` command is not found, install AWS CLI: https://aws.amazon.com/cli/ → Download the Windows installer → Run it → Restart your terminal.

### 12.3 Verify Everything

Open your browser:

```
https://<cloudfront-domain>.cloudfront.net
```

| Test | Expected Result |
|---|---|
| Page loads | React app with login page |
| Login | `sample-admin@example.com` / `SamplePass12` works |
| API health | `https://<cloudfront>.cloudfront.net/api/health` returns JSON |
| Create a booking | Form submits, data saved |
| Upload profile image | Image appears via CloudFront |
| Google OAuth2 | Redirects to Google, comes back with token |

---

## Step 13: Install Jenkins on Windows

### 13.1 Install Jenkins

1. Go to https://www.jenkins.io/download/ → Click **"Windows"** under LTS
2. Run the **MSI installer**
3. Choose installation directory (default is fine)
4. **Logon Type**: Run service as LocalSystem
5. **Port**: Change to `9090` (to avoid conflict with Spring Boot's 8080)
6. **Java home**: Browse to your JDK 21 installation (e.g., `C:\Program Files\Java\jdk-21`)
7. Complete the installation
8. Open browser → **http://localhost:9090**

### 13.2 Unlock Jenkins

9. The page shows "Unlock Jenkins" with a file path
10. Open that file in Notepad:
    ```
    C:\ProgramData\Jenkins\.jenkins\secrets\initialAdminPassword
    ```
    (or the path shown on the page)
11. Copy the password → Paste in the browser → Click **"Continue"**
12. Click **"Install suggested plugins"** → Wait for installation
13. **Create First Admin User**: Set your username, password, full name, email
14. **Jenkins URL**: `http://localhost:9090/` → Click **"Save and Finish"**
15. Click **"Start using Jenkins"**

### 13.3 Install Required Plugins

16. **Manage Jenkins** (left sidebar) → **Plugins** → **Available plugins** tab
17. Search and check each:
    - ✅ **SSH Agent**
    - ✅ **Pipeline: AWS Steps**
    - ✅ **NodeJS**
    - ✅ **GitHub Integration**
18. Click **"Install"**
19. Check ✅ **"Restart Jenkins when installation is complete and no jobs are running"**
20. Wait for restart → Log back in

### 13.4 Configure Global Tools

21. **Manage Jenkins** → **Tools**

**JDK installations:**
22. Click **"Add JDK"**
    - **Name**: `JDK-21`
    - ❌ Uncheck "Install automatically"
    - **JAVA_HOME**: `C:\Program Files\Java\jdk-21` (your actual path)

**NodeJS installations:**
23. Click **"Add NodeJS"**
    - **Name**: `NodeJS-22`
    - ✅ Check "Install automatically"
    - **Version**: Select `NodeJS 22.x.x` (latest)

24. Click **"Save"**

### 13.5 Add Credentials

25. **Manage Jenkins** → **Credentials** → Click on **"(global)"** under "Stores scoped to Jenkins" → **"Add Credentials"**

**Credential 1: EC2 SSH Key**

| Field | Value |
|---|---|
| Kind | SSH Username with private key |
| Scope | Global |
| ID | `ec2-ssh-key` |
| Username | `ec2-user` |
| Private Key | Enter directly → click "Add" → paste the ENTIRE contents of `smart-campus-key.pem` |

Click **"Create"**

**Credential 2: AWS Access Keys**

| Field | Value |
|---|---|
| Kind | AWS Credentials |
| ID | `aws-credentials` |
| Access Key ID | Your IAM access key |
| Secret Access Key | Your IAM secret key |

Click **"Create"**

**Credential 3: EC2 Host**

| Field | Value |
|---|---|
| Kind | Secret text |
| ID | `ec2-host` |
| Secret | Your Elastic IP (e.g., `13.233.xx.xx`) |

Click **"Create"**

**Credential 4: CloudFront Distribution ID**

| Field | Value |
|---|---|
| Kind | Secret text |
| ID | `cloudfront-distribution-id` |
| Secret | Your CloudFront distribution ID (e.g., `E1A2B3C4D5E6F7`) |

Click **"Create"**

---

## Step 14: Set Up ngrok Webhook Tunnel

### 14.1 Install ngrok

1. Go to https://ngrok.com/ → **Sign up** for a free account
2. Go to **Your Authtoken** page: https://dashboard.ngrok.com/get-started/your-authtoken
3. Copy your auth token
4. Download ngrok for Windows: https://ngrok.com/download
5. Extract `ngrok.exe` to a folder (e.g., `C:\tools\ngrok\`)
6. Open PowerShell:

```powershell
C:\tools\ngrok\ngrok.exe config add-authtoken <your-auth-token>
```

### 14.2 Claim Your Free Static Domain

> [!TIP]
> ngrok gives you **1 free static domain** so the URL doesn't change every restart!

7. Go to https://dashboard.ngrok.com/domains
8. Click **"Create Domain"** → You'll get something like `happy-robin-currently.ngrok-free.app`
9. **📝 Save** this domain name

### 14.3 Start the Tunnel

```powershell
C:\tools\ngrok\ngrok.exe http 9090 --domain=happy-robin-currently.ngrok-free.app
```

> [!NOTE]
> You need to keep this terminal **running** for webhooks to work. Consider creating a batch file to start it easily:
> ```batch
> @echo off
> C:\tools\ngrok\ngrok.exe http 9090 --domain=happy-robin-currently.ngrok-free.app
> ```
> Save as `start-ngrok.bat` on your desktop.

### 14.4 Configure GitHub Webhooks

**For the Backend repo:**

10. Go to your GitHub backend repo → **Settings** → **Webhooks** → **"Add webhook"**

| Field | Value |
|---|---|
| Payload URL | `https://happy-robin-currently.ngrok-free.app/github-webhook/` |
| Content type | `application/json` |
| Which events | Just the push event |
| Active | ✅ Checked |

11. Click **"Add webhook"**
12. GitHub will send a ping — verify it shows a ✅ green check

**For the Frontend repo:**

13. Repeat steps 10-12 for your frontend repo (same ngrok URL)

---

## Step 15: Create Jenkins Pipelines

### 15.1 Backend Pipeline

1. Jenkins Dashboard → **"New Item"** (left sidebar)
2. **Enter item name**: `smart-campus-backend`
3. Select **"Pipeline"** → Click **"OK"**
4. **General**:
    - ✅ Check **"GitHub project"**
    - **Project url**: `https://github.com/<your-org>/smart-campus-system-BackEnd/`
5. **Build Triggers**:
    - ✅ Check **"GitHub hook trigger for GITScm polling"**
6. **Pipeline**:
    - **Definition**: Pipeline script from SCM
    - **SCM**: Git
    - **Repository URL**: `https://github.com/<your-org>/smart-campus-system-BackEnd.git`
    - **Credentials**: Add if private repo (Kind: Username with password, use GitHub token as password)
    - **Branch Specifier**: `*/main`
    - **Script Path**: `Jenkinsfile`
7. Click **"Save"**

### 15.2 Frontend Pipeline

8. **"New Item"** → Name: `smart-campus-frontend` → **"Pipeline"** → **"OK"**
9. Same configuration, but:
    - **Project url**: Frontend repo URL
    - **Repository URL**: Frontend repo Git URL
    - **Script Path**: `Jenkinsfile`
10. Click **"Save"**

### 15.3 Run a Test Build

11. Go to `smart-campus-backend` pipeline → Click **"Build Now"**
12. Click on the **build number** (#1) in the Build History → **"Console Output"**
13. Watch it progress through: Checkout → Test → Build → Deploy → Health Check
14. If all stages pass: ✅ **Jenkins is fully set up!**

### 15.4 Test Webhook

15. Make a small change to your backend code (e.g., add a comment)
16. `git add .` → `git commit -m "test webhook"` → `git push`
17. Watch Jenkins — a new build should start automatically within seconds!

---

## ✅ Final Checklist

| # | Check | How to Verify |
|---|---|---|
| 1 | Frontend loads | Browse `https://<cloudfront>.cloudfront.net` |
| 2 | API works | Browse `https://<cloudfront>.cloudfront.net/api/health` |
| 3 | Login works | Login with `sample-admin@example.com` / `SamplePass12` |
| 4 | Google OAuth2 | Click "Login with Google" |
| 5 | Image upload | Upload a profile picture and verify it loads |
| 6 | Jenkins backend | Push to backend repo → Jenkins builds & deploys |
| 7 | Jenkins frontend | Push to frontend repo → Jenkins builds & deploys to S3 |
| 8 | Billing budget | AWS Budgets page shows `$0.00` |
| 9 | CloudWatch alarms | All 7 alarms in "OK" state |
| 10 | Notifications | Check you received the SNS subscription confirmation email |

---

## 🆘 Troubleshooting Quick Reference

| Symptom | Likely Cause | Fix |
|---|---|---|
| Cannot SSH into EC2 | SG missing port 22 from your IP | Edit `smart-campus-ec2-sg` inbound rules |
| `Permission denied (publickey)` | Wrong key file or permissions | `chmod 400 smart-campus-key.pem` |
| Spring Boot fails to start | Bad `.env` values | `sudo journalctl -u smart-campus -n 100` |
| `database: DOWN` in health check | RDS SG not allowing EC2, or wrong password | Check SG + password in `.env` |
| `redis: DOWN` in health check | Redis not running | `sudo systemctl start redis6` |
| CloudFront shows 403 | S3 bucket policy not updated | Re-do Step 9.5 |
| React routes show 403/404 | Missing error pages in CloudFront | Re-do Step 9.9 |
| OAuth2 redirect mismatch | Wrong redirect URI in Google Console | Re-do Step 11 |
| Jenkins webhook not firing | ngrok not running, or wrong URL | Check ngrok terminal, verify GitHub webhook shows ✅ |
| `aws` command not found | AWS CLI not installed | Download from https://aws.amazon.com/cli/ |
| S3 sync "Access Denied" | Wrong IAM policy or credentials | Verify IAM policy + `aws configure` |
| Unexpected AWS charges | Resource outside free tier | Check Cost Explorer, look for extra instances/snapshots |

---

## 📅 Weekly Maintenance Routine

1. **Monday**: Check **AWS Cost Explorer** (Billing → Cost Explorer) — should be $0.00
2. **Monday**: Check **CloudWatch alarms** — all should be "OK"
3. **Wednesday**: SSH into EC2:
   ```bash
   df -h                           # Check disk space
   sudo du -sh /opt/smart-campus/logs/  # Check log size
   redis6-cli info memory          # Check Redis memory
   ```
4. **Friday**: Review RDS console → Check **Free storage space**
5. **Monthly**: Check for old EBS snapshots (EC2 → Snapshots) and delete unused ones
